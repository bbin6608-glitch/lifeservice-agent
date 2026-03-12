---
id: kb-middleware-threadpool
title: 线程池与异步任务
category: middleware
tags: [threadpool, async, cache, canal]
usage_scope: [qa, ptest, troubleshooting]
source_files: [middleware_usage.txt, configs.txt]
---

# 线程池与异步任务

## 线程池类型
- `cacheRebuildExecutor`：用于缓存重建
- `canalExecutorService`：用于 Canal 监听消费

## 关键特征
- Canal 线程名格式：`canal-shop-worker-x`
- 线程池满时会记录拒绝日志

## 关联场景
- 商铺缓存重建
- Canal 增量监听
