---
id: kb-runtime-middleware-config
title: 中间件配置总览
category: runtime
tags: [mysql, redis, rabbitmq, canal]
usage_scope: [qa, ptest, troubleshooting]
source_files: [application.yaml]
---

# 中间件配置总览

## 数据源
- MySQL 驱动：`com.mysql.cj.jdbc.Driver`
- URL：`jdbc:mysql://127.0.0.1:3307/lifeservice?useSSL=false&serverTimezone=UTC`

## Redis
- host：`localhost`
- port：`6379`
- password：已配置

## RabbitMQ
- host：`localhost`
- port：`5672`
- exchange：`cache.fanout.exchange`
- queue：`${APP_QUEUE:cache.invalidate.queue.default}`

## Canal
- enabled：`${CANAL_ENABLED:true}`
- destination：`example`
- subscribe：`lifeservice\.tb_shop`
