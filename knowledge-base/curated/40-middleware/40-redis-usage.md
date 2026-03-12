---
id: kb-middleware-redis
title: Redis 使用专题
category: middleware
tags: [redis, stringredistemplate]
usage_scope: [qa, ptest, troubleshooting]
source_files: [redis_usage.txt]
---

# Redis 使用专题

## 用途分类
- 验证码与 Token：`UserServiceImpl`、`RefreshTokenInterceptor`
- 通用缓存：`CacheClient`、`VoucherServiceImpl`
- 秒杀控制：`VoucherOrderServiceImpl`
- 全局 ID：`RedisIdWorker`
- 启动预热：`SeckillStockWarmUpRunner`

## 典型能力
- `opsForValue`
- `opsForHash`
- `setIfAbsent`
- `execute(lua)`
- `delete`
