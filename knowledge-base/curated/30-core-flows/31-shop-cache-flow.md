---
id: kb-flow-shop-cache
title: 商铺缓存链路
category: core-flow
tags: [shop, cache, redis, rebuild]
usage_scope: [qa, ptest]
source_files: [project_summary.md, redis_usage.txt, middleware_usage.txt]
---

# 商铺缓存链路

## 一句话结论
商铺查询优先走 Redis 缓存，并结合空值缓存与逻辑过期策略防止缓存穿透和缓存击穿。

## 核心点
- `CacheClient` 负责缓存读写、空值缓存、锁控制和异步重建。
- 异步重建依赖线程池 `cacheRebuildExecutor`。
- 命中失败时才回源数据库。

## 压测关注
- 缓存命中率
- 回源数据库 RT
- 重建线程池饱和情况
