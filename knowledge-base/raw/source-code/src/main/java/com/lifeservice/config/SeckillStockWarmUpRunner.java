package com.lifeservice.config;

import com.lifeservice.cache.RedisConstants;
import com.lifeservice.entity.SeckillVoucher;
import com.lifeservice.service.ISeckillVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀库存预热：项目启动时，将未结束的秒杀券库存同步到 Redis
 */
@Slf4j
@Component
public class SeckillStockWarmUpRunner implements CommandLineRunner {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. 查询数据库中未结束的秒杀券 (endTime >= now)
        List<SeckillVoucher> seckillVouchers = seckillVoucherService.lambdaQuery()
                .ge(SeckillVoucher::getEndTime, LocalDateTime.now())
                .list();

        if (seckillVouchers == null || seckillVouchers.isEmpty()) {
            return;
        }

        // 2. 将库存重建到 Redis
        int count = 0;
        for (SeckillVoucher voucher : seckillVouchers) {
            // key = SECKILL_STOCK_KEY + voucherId
            String key = RedisConstants.SECKILL_STOCK_KEY + voucher.getVoucherId();
            // 写入时转成字符串，StringRedisTemplate 只能操作 String
            stringRedisTemplate.opsForValue().set(key, voucher.getStock().toString());
            count++;
        }

    }
}
