---
id: kb-source-cache-client
title: CacheClient 源码导读
category: source-guide
tags:
  - cache
  - redis
  - caffeine
  - logical-expire
  - pass-through
  - lock
usage_scope:
  - qa
  - ptest
source_files:
  - CacheClient.java
  - ThreadPoolConfig.java
confidence: high
---

# CacheClient 源码导读

## 一句话结论
`CacheClient` 是当前项目的缓存核心组件，实现了 Redis + Caffeine 二级缓存、空值缓存、防穿透、逻辑过期、防击穿和异步缓存重建。fileciteturn20file1turn20file5

## 背景/定位
它不是单纯的 Redis 工具类，而是一个面向业务查询模式的缓存策略组件。`ShopServiceImpl` 直接依赖它查询商铺。fileciteturn20file6turn20file1

## 关键事实
1. 同时依赖 `StringRedisTemplate` 和 `Cache<String, Object> caffeineCache`。fileciteturn20file1
2. `set()` 会给 TTL 叠加 60~300 秒随机值，做缓存打散。fileciteturn20file1
3. `setWithLogicalExpire()` 把真实数据包装进 `RedisData`，写入逻辑过期时间。fileciteturn20file1
4. `queryWithPassThrough()` 实现二级缓存查询与空值缓存。fileciteturn20file1
5. `queryWithLogicalExpire()` 在发现逻辑过期时，会尝试分布式锁，并把缓存重建任务提交到 `cacheRebuildExecutor`。fileciteturn20file1turn20file5
6. `unlock()` 使用 Lua 校验 token 后删除锁，避免误删他人锁。fileciteturn20file1

## 关键类 / 接口 / 配置
- 类：`CacheClient`
- 本地缓存：Caffeine `Cache<String, Object>`
- 线程池：`cacheRebuildExecutor`
- Redis 脚本：`UNLOCK_SCRIPT`
- 关键模式：
  - `queryWithPassThrough`
  - `queryWithLogicalExpire`。fileciteturn20file1turn20file5

## 链路说明 / 机制说明
### 防穿透链路
L1 Caffeine → L2 Redis → DB → 写回缓存；若 DB 返回空，则写入 `CACHE_NULL_VALUE`。fileciteturn20file1

### 逻辑过期链路
1. 读取 `RedisData`。
2. 未过期直接返回。
3. 已过期则尝试获取 `LOCK_KEY_PREFIX + key` 锁。
4. 获取锁成功后异步重建缓存。
5. 当前请求返回旧值。fileciteturn20file1

### 锁释放逻辑
`tryLock()` 存 token；`unlock()` 通过 Lua 比较 token 再删除，避免误删。fileciteturn20file1

## 对问答 agent 的价值
适合回答：
- 为什么要用逻辑过期
- 为什么要加本地 Caffeine
- 缓存重建为什么是异步的

## 对压测 agent 的价值
适合支撑：
- `/shop/{id}` 压测时为什么要关注缓存命中与重建线程池
- 为什么会出现尾延迟抖动

## 已确认事实
- `cacheRebuildExecutor` 来自 Spring 托管线程池。fileciteturn20file5turn20file1
- `delete(String key)` 会同步删除 Redis 和当前实例 Caffeine。fileciteturn20file1

## 合理推断
- 多实例场景下，仅删除当前实例的 Caffeine 不足以完成全局缓存失效，因此需要配合 MQ 广播或 Canal；当前项目的 `ShopCanalListener + CacheInvalidationPublisher` 正好说明了这一点。fileciteturn20file2turn20file3

## 来源依据
- `CacheClient.java`
- `ThreadPoolConfig.java`
- `ShopCanalListener.java`
