---
id: kb-method-voucher-order-seckillVoucher
title: VoucherOrderServiceImpl.seckillVoucher() 方法卡片
category: method-card
tags:
  - seckill
  - method
  - redis
  - lua
  - rabbitmq
usage_scope:
  - qa
  - ptest
source_files:
  - VoucherOrderServiceImpl.java
  - seckill.lua
confidence: high
---

# VoucherOrderServiceImpl.seckillVoucher() 方法卡片

## 一句话结论
这是秒杀入口的高性能方法：先用 Lua 把资格校验和预扣减做成 Redis 原子操作，再生成订单号并投递 MQ，最后极速返回。fileciteturn20file8turn20file0

## 背景/定位
方法签名：`public Result seckillVoucher(Long voucherId)`。这是秒杀请求最核心的方法。fileciteturn20file8

## 关键事实
- 当前用户来自 `UserHolder.getUser().getId()`。fileciteturn20file8
- 执行 Lua 时参数顺序为 `voucherId, userId`。fileciteturn20file8turn20file0
- Lua 返回非 0 时直接失败返回。fileciteturn20file8
- 成功时生成订单号、发 MQ、返回 `Result.ok(orderId)`。fileciteturn20file8turn20file9

## 关键类 / 接口 / 配置
- `StringRedisTemplate`
- `DefaultRedisScript<Long>`
- `RedisIdWorker`
- `RabbitTemplate`
- `RabbitMqConfig`。fileciteturn20file8turn20file3turn20file9

## 链路说明 / 机制说明
1. 用户鉴权已在更前置的拦截器层完成。
2. 方法内只做抢购资格原子校验。
3. 成功后立即返回，避免数据库写成为入口瓶颈。
4. MQ 发送失败则做 Redis 补偿。fileciteturn20file8

## 对问答 agent 的价值
适合回答“秒杀入口具体做了什么”。

## 对压测 agent 的价值
适合说明为什么秒杀入口压测要重点看 Redis、MQ 和补偿链路。

## 已确认事实
- 该方法本身不直接写数据库。fileciteturn20file8
- 订单 ID 在 MQ 发送前生成。fileciteturn20file8turn20file9

## 合理推断
- 若 MQ 大量失败，Redis 补偿逻辑会被频繁触发，可能影响一致性和系统抖动。

## 来源依据
- `VoucherOrderServiceImpl.java`
- `seckill.lua`
