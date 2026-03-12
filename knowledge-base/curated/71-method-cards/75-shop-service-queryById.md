---
id: kb-method-shopservice-queryById
title: ShopServiceImpl.queryById() 方法卡片
category: method-card
tags:
  - shop
  - method
  - cache
usage_scope:
  - qa
  - ptest
source_files:
  - ShopServiceImpl.java
  - CacheClient.java
confidence: high
---

# ShopServiceImpl.queryById() 方法卡片

## 一句话结论
这是商铺详情查询入口，直接走 `CacheClient.queryWithLogicalExpire()`，说明项目将商铺详情视为热点缓存数据。fileciteturn20file6turn20file1

## 背景/定位
方法签名：`public Result queryById(Long id)`。fileciteturn20file6

## 关键事实
- 使用 `CACHE_SHOP_KEY + id` 作为缓存键。
- 未命中或不存在时返回失败提示。
- 命中后返回 `Result.ok(shop)`。fileciteturn20file6

## 关键类 / 接口 / 配置
- `CacheClient`
- `Shop`
- `CACHE_SHOP_TTL`。fileciteturn20file6

## 链路说明 / 机制说明
本方法本身很薄，真正的复杂度被下沉到 `CacheClient`，这是一种典型的“业务服务薄、缓存策略厚”的实现。fileciteturn20file6turn20file1

## 对问答 agent 的价值
适合回答“商铺查询入口在哪里”。

## 对压测 agent 的价值
适合解释 `/shop/{id}` 为什么是热点读链路。

## 已确认事实
- 查询逻辑不是直查数据库。fileciteturn20file6

## 合理推断
- 若本地缓存与 Redis 同时失效，该接口会对 DB 形成回源压力。

## 来源依据
- `ShopServiceImpl.java`
- `CacheClient.java`
