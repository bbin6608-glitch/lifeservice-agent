---
id: kb-api-catalog
title: API 总览
category: api
tags: [api, user, shop, voucher, voucher-order, blog, follow]
usage_scope: [qa, ptest]
source_files: [api_mappings.txt]
---

# API 总览

## 用户类接口
- `/user/code`
- `/user/login`
- `/user/logout`
- `/user/me`
- `/user/info/{id}`
- `/user/{id}`
- `/user/sign`
- `/user/sign/count`

## 商铺类接口
- `/shop/{id}`
- `/shop/of/type`
- `/shop/of/name`

## 优惠券类接口
- `/voucher`
- `/voucher/seckill`
- `/voucher/list/{shopId}`

## 秒杀下单接口
- `/voucher-order/seckill/{id}`

## 内容类接口
- `/blog`
- `/blog/hot`
- `/follow`
- `/blog-comments`
