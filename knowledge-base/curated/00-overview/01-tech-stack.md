---
id: kb-overview-tech-stack
title: 技术栈与中间件
category: overview
tags: [spring-boot, mybatis-plus, redis, rabbitmq, canal, caffeine]
usage_scope: [qa, ptest]
source_files: [pom.xml, project_summary.md, redis_usage.txt, middleware_usage.txt, lua_refs.txt]
---

# 技术栈与中间件

## 核心框架
- Spring Boot
- MyBatis-Plus
- MySQL

## 缓存与原子控制
- Redis 由 `StringRedisTemplate` 贯穿多个关键类
- Lua 脚本用于解锁和秒杀原子校验
- Caffeine 用于本地缓存配置

## 异步与解耦
- RabbitMQ 用于缓存失效广播和秒杀订单异步下单

## Binlog 监听
- Canal 用于监听 `lifeservice.tb_shop` 变更并参与缓存失效

## 工具能力
- RedisIdWorker 负责全局 ID 生成
- 线程池用于缓存重建和 Canal 消费
