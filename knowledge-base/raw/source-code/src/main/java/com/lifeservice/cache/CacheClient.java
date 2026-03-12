package com.lifeservice.cache;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.lifeservice.cache.RedisConstants.*;

/**
 * 缓存客户端工具
 */
@Slf4j
@Component
public class CacheClient {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Cache<String, Object> caffeineCache;

    @Resource
    private ExecutorService cacheRebuildExecutor;

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setResultType(Long.class);
        UNLOCK_SCRIPT.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]) " +
                "else " +
                "return 0 " +
                "end"
        );
    }

    /**
     * 设置普通缓存（带 TTL 随机打散）
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        if (value == null) {
            throw new IllegalArgumentException("Cache value cannot be null!");
        }
        long ttlSeconds = unit.toSeconds(time) + RandomUtil.randomLong(60, 300);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), ttlSeconds, TimeUnit.SECONDS);
        caffeineCache.put(key, value);
    }

    /**
     * 设置逻辑过期缓存
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        if (value == null) {
            throw new IllegalArgumentException("Logical expire data cannot be null!");
        }
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        
        String json = JSONUtil.toJsonStr(redisData);
        stringRedisTemplate.opsForValue().set(key, json);
        caffeineCache.put(key, redisData);
    }

    /**
     * 二级缓存查询：解决缓存穿透
     */
    public <R, ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        
        // 1. L1
        Object cachedObj = caffeineCache.getIfPresent(key);
        if (cachedObj != null) {
            if (CACHE_NULL_VALUE.equals(cachedObj)) return null;
            return type.cast(cachedObj);
        }

        // 2. L2
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            if (CACHE_NULL_VALUE.equals(json)) {
                caffeineCache.put(key, CACHE_NULL_VALUE);
                return null;
            }
            R r = JSONUtil.toBean(json, type);
            caffeineCache.put(key, r);
            return r;
        }
        
        if (json != null) {
            caffeineCache.put(key, CACHE_NULL_VALUE);
            return null;
        }

        // 3. DB
        R result = dbFallback.apply(id);
        if (result == null) {
            stringRedisTemplate.opsForValue().set(key, CACHE_NULL_VALUE, CACHE_NULL_TTL, TimeUnit.MINUTES);
            caffeineCache.put(key, CACHE_NULL_VALUE);
            return null;
        }

        this.set(key, result, time, unit);
        return result;
    }

    /**
     * 二级缓存查询：逻辑过期
     */
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;

        RedisData redisData = getRedisDataSafe(keyPrefix, id, type, dbFallback, time, unit);
        
        if (redisData == null) {
            return null; 
        }

        R result = JSONUtil.toBean(JSONUtil.toJsonStr(redisData.getData()), type);
        LocalDateTime expireTime = redisData.getExpireTime();

        if (expireTime.isAfter(LocalDateTime.now())) {
            return result; 
        }

        String lockKey = LOCK_KEY_PREFIX + key;
        String token = tryLock(lockKey);
        if (token != null) {
            try {
                cacheRebuildExecutor.submit(() -> {
                    try {
                        R r1 = dbFallback.apply(id);
                        if (r1 == null) {
                            stringRedisTemplate.opsForValue().set(key, CACHE_NULL_VALUE, CACHE_NULL_TTL, TimeUnit.MINUTES);
                            caffeineCache.put(key, CACHE_NULL_VALUE);
                        } else {
                            this.setWithLogicalExpire(key, r1, time, unit);
                        }
                    } catch (Exception e) {
                        log.error("Cache rebuild error", e);
                    } finally {
                        unlock(lockKey, token);
                    }
                });
            } catch (Exception e) {
                unlock(lockKey, token);
            }
        }

        return result; 
    }

    private <R, ID> RedisData getRedisDataSafe(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        
        Object obj = caffeineCache.getIfPresent(key);
        if (obj instanceof RedisData) return (RedisData) obj;
        if (CACHE_NULL_VALUE.equals(obj)) return null;

        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            if (CACHE_NULL_VALUE.equals(json)) {
                caffeineCache.put(key, CACHE_NULL_VALUE);
                return null;
            }
            RedisData rd = JSONUtil.toBean(json, RedisData.class);
            caffeineCache.put(key, rd);
            return rd;
        }
        
        if (json != null) {
            caffeineCache.put(key, CACHE_NULL_VALUE);
            return null;
        }

        R dbResult = dbFallback.apply(id);
        if (dbResult == null) {
            stringRedisTemplate.opsForValue().set(key, CACHE_NULL_VALUE, CACHE_NULL_TTL, TimeUnit.MINUTES);
            caffeineCache.put(key, CACHE_NULL_VALUE);
            return null;
        }

        RedisData rd = new RedisData();
        rd.setData(dbResult);
        rd.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        this.setWithLogicalExpire(key, dbResult, time, unit);
        return rd;
    }

    private String tryLock(String key) {
        String token = UUID.randomUUID().toString(true);
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, token, 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag) ? token : null;
    }

    private void unlock(String key, String token) {
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
    }

    /**
     * 失效缓存逻辑
     * 当前实现：同步删除 Redis(L2) 和当前实例本地 Caffeine(L1)。
     */
    public void delete(String key) {
        stringRedisTemplate.delete(key);
        caffeineCache.invalidate(key);
    }
}
