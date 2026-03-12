---
id: kb-source-rabbitmq-config
title: RabbitMqConfig 源码导读
category: source-guide
tags:
  - rabbitmq
  - fanout
  - direct
  - cache-invalidation
  - seckill
usage_scope:
  - qa
  - ptest
source_files:
  - RabbitMqConfig.java
confidence: high
---

# RabbitMqConfig 源码导读

## 一句话结论
`RabbitMqConfig` 同时配置了两条消息通道：一条用于缓存失效广播，一条用于秒杀订单异步处理。fileciteturn20file3

## 背景/定位
该类是 RabbitMQ 的核心装配点，直接定义了交换机、队列、绑定关系以及消息 JSON 序列化器。fileciteturn20file3

## 关键事实
1. 缓存广播使用 `FanoutExchange`，名称来自 `app.rabbitmq.exchange`，默认是 `cache.fanout.exchange`。fileciteturn20file3
2. 缓存广播队列名称来自 `app.rabbitmq.queue`。fileciteturn20file3
3. 秒杀链路使用独立的 `DirectExchange`：
   - `SECKILL_EXCHANGE = seckill.order.exchange`
   - `SECKILL_QUEUE = seckill.order.queue`
   - `SECKILL_ROUTING_KEY = seckill.order`。fileciteturn20file3
4. 秒杀订单队列是持久化队列。fileciteturn20file3
5. 配置了 `Jackson2JsonMessageConverter` 作为统一消息转换器。fileciteturn20file3

## 关键类 / 接口 / 配置
- 配置类：`RabbitMqConfig`
- 交换机：
  - `FanoutExchange`
  - `DirectExchange`
- 队列：
  - `cacheQueue`
  - `seckillQueue`。fileciteturn20file3

## 链路说明 / 机制说明
### 缓存广播链路
通过 fanout 交换机广播缓存失效消息，每个实例消费自己的队列，实现多实例本地缓存同步清理。fileciteturn20file3

### 秒杀异步下单链路
秒杀入口把消息按 routing key `seckill.order` 投递到 direct exchange，由 `seckill.order.queue` 消费。fileciteturn20file3turn20file8

## 对问答 agent 的价值
适合回答：
- RabbitMQ 在项目里用了几条链路
- 为什么缓存失效适合 fanout
- 为什么秒杀适合 direct + routing key

## 对压测 agent 的价值
适合支撑：
- 为什么秒杀压测要看 MQ 积压
- 为什么多实例缓存失效诊断要看广播链路

## 已确认事实
- 两类 MQ 配置共存在一个类中。fileciteturn20file3
- 秒杀使用的是 direct exchange，不是 fanout。fileciteturn20file3

## 合理推断
- 缓存广播队列被设置为非持久、独占、自动删除，更适合实例级临时监听而不是长期业务消息持久化。该推断来自队列构造参数。fileciteturn20file3

## 来源依据
- `RabbitMqConfig.java`
- `VoucherOrderServiceImpl.java`
