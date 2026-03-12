---
id: kb-source-shop-service
title: ShopServiceImpl 源码导读
category: source-guide
tags:
  - shop
  - cache
  - redis
  - logical-expire
usage_scope:
  - qa
  - ptest
source_files:
  - ShopServiceImpl.java
  - CacheClient.java
confidence: high
---

# ShopServiceImpl 源码导读

## 一句话结论
`ShopServiceImpl` 是商铺读取与更新的薄服务层，读取链路高度依赖 `CacheClient.queryWithLogicalExpire()`，说明商铺详情属于热点缓存场景。fileciteturn20file6turn20file1

## 背景/定位
该类位于 `service.impl`，主要暴露两个关键动作：
- `queryById(Long id)`：按店铺 ID 查询
- `update(Shop shop)`：更新店铺。fileciteturn20file6

## 关键事实
1. `queryById()` 通过 `cacheClient.queryWithLogicalExpire()` 查询店铺，而不是直接查数据库。fileciteturn20file6
2. 使用的 key 前缀是 `CACHE_SHOP_KEY`，TTL 使用 `CACHE_SHOP_TTL`。fileciteturn20file6
3. 如果店铺不存在，返回 `Result.fail("店铺不存在！")`。fileciteturn20file6
4. `update(Shop shop)` 标记为 `@Transactional`，但当前方法仅做数据库更新，不直接在此处执行缓存删除。fileciteturn20file6

## 关键类 / 接口 / 配置
- 类：`ShopServiceImpl`
- 依赖：`CacheClient`
- Redis 相关常量：`CACHE_SHOP_KEY`、`CACHE_SHOP_TTL`
- 关联机制：逻辑过期、本地 Caffeine + Redis 二级缓存。fileciteturn20file6turn20file1

## 链路说明 / 机制说明
### 商铺查询
1. 业务层调用 `queryById(id)`。
2. `CacheClient` 先查本地 Caffeine，再查 Redis。
3. 若命中逻辑过期对象但已过期，则尝试加锁并异步重建，当前请求返回旧值。fileciteturn20file1turn20file6

### 商铺更新
1. 校验 `id != null`。
2. 执行 `updateById(shop)`。
3. 当前方法本身不见显式缓存失效逻辑，因此缓存清理大概率依赖外围机制，例如 Canal + MQ 广播。fileciteturn20file6turn20file2turn20file3

## 对问答 agent 的价值
适合回答：
- 商铺缓存为什么是热点链路
- 逻辑过期在哪里使用
- 店铺更新后缓存怎么处理

## 对压测 agent 的价值
适合支撑：
- `/shop/{id}` 为什么适合做缓存命中压测
- 为什么要关注缓存重建线程池与数据库回源

## 已确认事实
- `queryById()` 明确调用 `queryWithLogicalExpire()`。fileciteturn20file6
- `CacheClient` 中存在逻辑过期与异步缓存重建实现。fileciteturn20file1

## 合理推断
- 店铺更新后的缓存一致性在当前系统中更可能由 Canal 监听 `tb_shop` 变更并通过 MQ 广播失效，而不是在 `ShopServiceImpl.update()` 内部直接删除缓存。该推断来自 `ShopCanalListener` 和 `RabbitMqConfig` 的现有实现。fileciteturn20file2turn20file3

## 来源依据
- `ShopServiceImpl.java`
- `CacheClient.java`
- `ShopCanalListener.java`
- `RabbitMqConfig.java`
