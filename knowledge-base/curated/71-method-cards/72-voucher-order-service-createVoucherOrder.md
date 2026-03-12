---
id: kb-method-voucher-order-createVoucherOrder
title: VoucherOrderServiceImpl.createVoucherOrder() 方法卡片
category: method-card
tags:
  - seckill
  - method
  - db
  - optimistic-lock
  - order
usage_scope:
  - qa
  - ptest
source_files:
  - VoucherOrderServiceImpl.java
confidence: high
---

# VoucherOrderServiceImpl.createVoucherOrder() 方法卡片

## 一句话结论
这是秒杀异步落库的事务方法，负责 DB 一人一单兜底、库存扣减和订单落表。fileciteturn20file8

## 背景/定位
方法签名：`public void createVoucherOrder(SeckillOrderMessage message)`，带 `@Transactional`。fileciteturn20file8

## 关键事实
- 先查订单表做“一人一单”兜底。fileciteturn20file8
- 用 `gt("stock", 0)` 做库存扣减保护。fileciteturn20file8
- 库存扣减失败会抛出 `RuntimeException("数据库库存扣减失败")`。fileciteturn20file8
- 最后保存 `VoucherOrder` 实体。fileciteturn20file8

## 关键类 / 接口 / 配置
- `ISeckillVoucherService`
- `VoucherOrder`
- `SeckillOrderMessage`。fileciteturn20file8

## 链路说明 / 机制说明
该方法是 Redis 预扣减之后的物理库存兜底点，也是消息消费真正完成业务闭环的位置。fileciteturn20file8

## 对问答 agent 的价值
适合回答“数据库兜底在哪做”。

## 对压测 agent 的价值
适合解释为什么压测时还要关注 DB 乐观锁冲突。

## 已确认事实
- 本方法会直接访问数据库。fileciteturn20file8

## 合理推断
- 如果消息重试或重复投递，该方法的“一人一单 DB 兜底”承担了幂等保护作用。

## 来源依据
- `VoucherOrderServiceImpl.java`
