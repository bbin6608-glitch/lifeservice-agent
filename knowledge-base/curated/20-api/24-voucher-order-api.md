---
id: kb-api-voucher-order
title: 秒杀下单接口
category: api
tags: [voucher-order, seckill, redis, lua, rabbitmq]
usage_scope: [qa, ptest]
source_files: [api_mappings.txt, project_summary.md, redis_usage.txt, middleware_usage.txt, lua_refs.txt]
---

# 秒杀下单接口

## 核心入口
- `POST /voucher-order/seckill/{id}`

## 关键特征
- 高并发入口
- 依赖 Redis Lua 原子校验
- 依赖 RedisIdWorker 生成订单号
- 依赖 RabbitMQ 异步下单

## 压测优先级
P0，优先于其他接口进行压测设计与结果分析。
