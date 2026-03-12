---
id: kb-source-voucher-order-service
title: VoucherOrderServiceImpl 源码导读
category: source-guide
tags:
  - seckill
  - voucher-order
  - redis
  - lua
  - rabbitmq
  - db
usage_scope:
  - qa
  - ptest
source_files:
  - VoucherOrderServiceImpl.java
  - seckill.lua
  - RabbitMqConfig.java
  - RedisIdWorker.java
confidence: high
---

# VoucherOrderServiceImpl 源码导读

## 一句话结论
`VoucherOrderServiceImpl` 是秒杀下单链路的核心编排类：入口方法先在 Redis 中通过 Lua 完成资格校验与库存预扣减，然后生成全局订单号并投递 RabbitMQ，真实订单落库由后续事务方法执行。

## 背景/定位
该类位于 `service.impl`，继承 `ServiceImpl<VoucherOrderMapper, VoucherOrder>` 并实现 `IVoucherOrderService`。它不是一个普通的同步下单服务，而是一个“Redis 快路径 + MQ 异步落库 + DB 兜底”的高并发订单服务。fileciteturn20file8

## 关键事实
1. 入口方法为 `seckillVoucher(Long voucherId)`，先取当前登录用户 ID。fileciteturn20file8
2. 通过 `StringRedisTemplate.execute(SECKILL_SCRIPT, ...)` 执行 `seckill.lua`，原子完成库存校验、一人一单、预扣减。fileciteturn20file8turn20file0
3. Lua 返回码含义明确：
   - `0` 成功
   - `1` 库存不足
   - `2` 重复下单
   - `3` 库存未初始化。fileciteturn20file8turn20file0
4. 校验通过后，使用 `RedisIdWorker.nextId("order")` 生成订单号。fileciteturn20file8turn20file9
5. 异步消息通过 `RabbitTemplate.convertAndSend()` 发往 `RabbitMqConfig.SECKILL_EXCHANGE` 和 `SECKILL_ROUTING_KEY`。fileciteturn20file8turn20file3
6. `createVoucherOrder(SeckillOrderMessage message)` 是后台事务下单逻辑，负责 DB 一人一单兜底、库存扣减和订单落表。fileciteturn20file8
7. MQ 投递失败时会调用 `compensateRedis()` 回滚 Redis 中的库存和已购资格集合。fileciteturn20file8

## 关键类 / 接口 / 配置
- 类：`VoucherOrderServiceImpl`
- Lua：`seckill.lua`
- 工具：`RedisIdWorker`
- MQ 配置：`RabbitMqConfig`
- 依赖：
  - `ISeckillVoucherService`
  - `StringRedisTemplate`
  - `RabbitTemplate`
  - `UserHolder`。fileciteturn20file8turn20file3turn20file9

## 链路说明 / 机制说明
### 入口快路径
1. 获取用户。
2. 执行 Lua。
3. 成功则生成订单 ID。
4. 发送 MQ 消息。
5. 立即返回订单 ID。fileciteturn20file8turn20file0

### 后台事务路径
1. 根据消息拿到 `voucherId / userId / orderId`。
2. 查询订单表做“一人一单”兜底。
3. 通过 `setSql("stock = stock - 1").gt("stock", 0)` 做 MySQL 乐观锁式扣减。
4. 保存 `VoucherOrder`。fileciteturn20file8

### Redis 补偿路径
当 MQ 投递失败时，使用 `increment(stockKey)` 回补库存，并从 `seckill:order:{voucherId}` 集合移除用户。fileciteturn20file8

## 对问答 agent 的价值
适合回答：
- 秒杀链路怎么走
- Lua 在哪里执行
- 订单 ID 如何生成
- MQ 在秒杀链路里做什么
- DB 兜底在哪里做

## 对压测 agent 的价值
适合支撑：
- `/voucher-order/seckill/{id}` 为何是 P0 接口
- 为什么要重点关注 Redis / Lua / RabbitMQ / DB 冲突
- 为什么要观察 MQ 积压和库存一致性

## 已确认事实
- 该类直接加载 `seckill.lua` 作为 `DefaultRedisScript<Long>`。fileciteturn20file8
- Lua 使用 `seckill:stock:{voucherId}` 和 `seckill:order:{voucherId}` 作为关键 Redis 键。fileciteturn20file0
- MQ 交换机和路由键分别是 `seckill.order.exchange`、`seckill.order`。fileciteturn20file3
- `createVoucherOrder()` 标记了 `@Transactional`。fileciteturn20file8

## 合理推断
- 当前类本身更像“下单编排器”，真实消费入口应在独立 Consumer 中触发 `createVoucherOrder()`；本次材料未包含该 Consumer 源码。基于现有方法命名和注释推断。fileciteturn20file8
- 由于入口先返回订单 ID，再异步落库，前端或调用方应接受“快速受理、异步完成”的链路特性。

## 来源依据
- `VoucherOrderServiceImpl.java`
- `seckill.lua`
- `RabbitMqConfig.java`
- `RedisIdWorker.java`
