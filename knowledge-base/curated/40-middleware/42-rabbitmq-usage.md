---
id: kb-middleware-rabbitmq
title: RabbitMQ 使用专题
category: middleware
tags: [rabbitmq, async, broadcast]
usage_scope: [qa, ptest, troubleshooting]
source_files: [application.yaml, middleware_usage.txt, configs.txt]
---

# RabbitMQ 使用专题

## 核心用途
- 缓存失效广播
- 秒杀订单异步下单

## 关键类
- `CacheInvalidationPublisher`
- `VoucherOrderServiceImpl`
- `RabbitMqConfig`

## 关键配置
- 交换机：`cache.fanout.exchange`
- 队列：按 profile 区分实例队列
