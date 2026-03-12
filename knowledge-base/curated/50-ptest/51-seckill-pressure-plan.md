---
id: kb-ptest-seckill-plan
title: 秒杀接口压测方案
category: ptest
tags: [ptest, seckill, redis, lua, rabbitmq]
usage_scope: [ptest]
source_files: [project_summary.md, redis_usage.txt, lua_refs.txt, middleware_usage.txt]
---

# 秒杀接口压测方案

## 目标接口
`POST /voucher-order/seckill/{id}`

## 前置条件
- Redis 就绪
- RabbitMQ 就绪
- MySQL 就绪
- 已完成秒杀库存预热

## 重点观察指标
- TPS
- P95 / P99 RT
- 错误率
- MQ 积压
- 库存一致性

## 重点组件
- `seckill.lua`
- `VoucherOrderServiceImpl`
- `RedisIdWorker`
- RabbitMQ 消费者
