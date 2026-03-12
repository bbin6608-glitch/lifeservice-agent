---
id: kb-ptest-shop-plan
title: 商铺读接口压测方案
category: ptest
tags: [ptest, shop, cache]
usage_scope: [ptest]
source_files: [project_summary.md, redis_usage.txt, middleware_usage.txt]
---

# 商铺读接口压测方案

## 目标接口
- `GET /shop/{id}`
- `GET /shop/of/type`
- `GET /shop/of/name`

## 重点观察指标
- 缓存命中率
- RT 波动
- 回源数据库压力
- 缓存重建线程池状态
