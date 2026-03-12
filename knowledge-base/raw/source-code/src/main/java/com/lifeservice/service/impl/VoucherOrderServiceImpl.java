package com.lifeservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeservice.cache.RedisConstants;
import com.lifeservice.config.RabbitMqConfig;
import com.lifeservice.dto.Result;
import com.lifeservice.dto.SeckillOrderMessage;
import com.lifeservice.entity.VoucherOrder;
import com.lifeservice.mapper.VoucherOrderMapper;
import com.lifeservice.service.ISeckillVoucherService;
import com.lifeservice.service.IVoucherOrderService;
import com.lifeservice.utils.RedisIdWorker;
import com.lifeservice.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * 优惠券订单服务实现类 (最终版：Redis Lua + RabbitMQ + 乐观锁)
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    /**
     * 秒杀下单入口 (高性能版)
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1. 获取用户
        Long userId = UserHolder.getUser().getId();
        // 2. 执行 Lua 脚本 (原子校验库存、一人一单、预扣库存)
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString()
        );
        int r = result.intValue();
        if (r != 0) {
            String msg = "未知错误";
            switch (r) {
                case 1: msg = "库存不足"; break;
                case 2: msg = "不能重复下单"; break;
                case 3: msg = "秒杀券库存未初始化，请联系管理员"; break;
            }
            return Result.fail(msg);
        }

        // 3. 校验通过，生成全局唯一 ID
        long orderId = redisIdWorker.nextId("order");

        // 4. 发送异步下单消息到 RabbitMQ
        SeckillOrderMessage message = new SeckillOrderMessage(orderId, userId, voucherId);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.SECKILL_EXCHANGE,
                    RabbitMqConfig.SECKILL_ROUTING_KEY,
                    message
            );
        } catch (Exception e) {
            log.error("消息投递 MQ 失败，直接触发 Redis 补偿！orderId: {}", orderId, e);
            compensateRedis(voucherId, userId);
            return Result.fail("下单请求发送失败，请重试");
        }

        // 5. 极速返回订单 ID，无需等待数据库操作
        return Result.ok(orderId);
    }

    /**
     * 【后台任务】真实下单事务逻辑 (由 Consumer 触发)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createVoucherOrder(SeckillOrderMessage message) {
        Long voucherId = message.getVoucherId();
        Long userId = message.getUserId();
        long orderId = message.getOrderId();

        // 1. 数据库一人一单兜底 (防止多节点、MQ 重试导致重复订单)
        Long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            log.warn("一人一单 DB 兜底：用户 {} 已下单过券 {}", userId, voucherId);
            return; // 已经下过单，幂等处理直接返回
        }

        // 2. MySQL 乐观锁扣减库存 (物理库存绝对安全)
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId).gt("stock", 0)
                .update();
        if (!success) {
            log.warn("库存扣减 DB 兜底失败：券 {} 已售罄", voucherId);
            // 这里建议抛异常，让 Consumer 捕捉到并执行补偿 (Redis 与 DB 最终一致)
            throw new RuntimeException("数据库库存扣减失败");
        }

        // 3. 写入订单表
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
    }

    /**
     * 【关键】Redis 补偿
     */
    @Override
    public void compensateRedis(Long voucherId, Long userId) {
        String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucherId;
        String orderKey = RedisConstants.SECKILL_ORDER_KEY + voucherId;
        stringRedisTemplate.opsForValue().increment(stockKey);
        stringRedisTemplate.opsForSet().remove(orderKey, userId.toString());
        log.info("已完成 Redis 库存与用户购票资格回滚。voucherId: {}, userId: {}", voucherId, userId);
    }
}
