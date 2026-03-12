---
id: kb-ptest-bottleneck-rules
title: 压测瓶颈诊断规则
category: ptest
tags: [ptest, diagnosis, redis, mq, cache, canal]
usage_scope: [ptest, troubleshooting]
source_files: [framework.txt, project_summary.md, middleware_usage.txt, redis_usage.txt, lua_refs.txt, application-app1.yaml, application-app2.yaml]
---

# 压测瓶颈诊断规则

## 规则 1
秒杀接口 RT 高、TPS 上不去、错误率不高：优先排查 Redis、Lua 和队列削峰链路。

## 规则 2
秒杀成功率下降且 MQ 消费滞后：优先排查异步下单消费侧。

## 规则 3
`/shop/{id}` RT 抖动明显：优先排查缓存命中、重建线程池和数据库回源。

## 规则 4
app1 与 app2 表现差异大：优先检查 Canal 开关和缓存失效广播路径。
