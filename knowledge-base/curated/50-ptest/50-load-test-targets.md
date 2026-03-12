---
id: kb-ptest-load-targets
title: 压测目标接口分级
category: ptest
tags: [ptest, api, seckill, shop, login]
usage_scope: [ptest]
source_files: [api_mappings.txt, project_summary.md, framework.txt]
---

# 压测目标接口分级

## P0
- `POST /voucher-order/seckill/{id}`：秒杀核心入口

## P1
- `GET /shop/{id}`：热点缓存查询
- `GET /shop/of/type`
- `GET /shop/of/name`
- `POST /user/code`
- `POST /user/login`

## P2
- `GET /voucher/list/{shopId}`
- `GET /blog/hot`
