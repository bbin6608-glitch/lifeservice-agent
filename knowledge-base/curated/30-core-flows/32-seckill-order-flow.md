---
id: kb-flow-seckill-order
title: 秒杀下单链路
category: core-flow
tags: [seckill, redis, lua, rabbitmq, voucher-order]
usage_scope: [qa, ptest]
source_files: [project_summary.md, redis_usage.txt, lua_refs.txt, middleware_usage.txt]
---

# 秒杀下单链路

## 执行步骤
1. 系统启动时，`SeckillStockWarmUpRunner` 将秒杀库存预热到 Redis。
2. 用户请求 `POST /voucher-order/seckill/{id}`。
3. `VoucherOrderServiceImpl` 执行 `seckill.lua` 完成库存校验、一人一单和预扣减。
4. 校验通过后，使用 `RedisIdWorker` 生成订单 ID。
5. 订单消息发送到 RabbitMQ。
6. 消费者异步落库，并在数据库侧做兜底校验。

## 高并发控制点
- Lua 原子脚本
- Redis 预扣库存
- RabbitMQ 异步削峰
- DB 乐观锁兜底

## 压测关注点
- RT / TPS
- 错误率
- MQ 积压
- 库存一致性
