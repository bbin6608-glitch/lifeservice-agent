---
id: kb-api-shop
title: 商铺接口
category: api
tags: [shop, cache]
usage_scope: [qa, ptest]
source_files: [api_mappings.txt, project_summary.md]
---

# 商铺接口

## 主要接口
- `GET /shop/{id}`
- `GET /shop/of/type`
- `GET /shop/of/name`

## 特点
这是典型的热点读接口，适合验证缓存命中、回源和重建行为。
