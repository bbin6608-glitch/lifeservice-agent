---
id: kb-method-cacheclient-queryWithLogicalExpire
title: CacheClient.queryWithLogicalExpire() 方法卡片
category: method-card
tags:
  - cache
  - logical-expire
  - method
  - rebuild
usage_scope:
  - qa
  - ptest
source_files:
  - CacheClient.java
  - ThreadPoolConfig.java
confidence: high
---

# CacheClient.queryWithLogicalExpire() 方法卡片

## 一句话结论
该方法实现热点数据的逻辑过期缓存：读请求允许拿旧值，同时尝试异步重建，避免缓存击穿。fileciteturn20file1turn20file5

## 背景/定位
方法签名：`public <R, ID> R queryWithLogicalExpire(...)`。商铺查询直接依赖它。fileciteturn20file1turn20file6

## 关键事实
- 如果缓存未过期，直接返回结果。fileciteturn20file1
- 如果缓存过期，尝试获取 `LOCK_KEY_PREFIX + key` 锁。fileciteturn20file1
- 获取锁成功则向 `cacheRebuildExecutor` 提交重建任务。fileciteturn20file1turn20file5
- 当前请求即使遇到过期，也返回旧值而不是阻塞等待。fileciteturn20file1

## 关键类 / 接口 / 配置
- `RedisData`
- `ExecutorService cacheRebuildExecutor`
- `UNLOCK_SCRIPT`。fileciteturn20file1

## 链路说明 / 机制说明
逻辑过期是“可接受短暂旧值换取高并发稳定”的策略，适合热点读场景。fileciteturn20file1

## 对问答 agent 的价值
适合解释“逻辑过期”和“异步重建”到底如何实现。

## 对压测 agent 的价值
适合支撑商铺查询压测时对尾延迟与缓存重建线程池的判断。

## 已确认事实
- 锁持有 token 是 UUID。fileciteturn20file1
- 锁过期时间为 10 秒。fileciteturn20file1

## 合理推断
- 若热点 key 大量同时过期，即使返回旧值，线程池仍可能在短时间承压，因此需要关注重建线程池队列。

## 来源依据
- `CacheClient.java`
- `ThreadPoolConfig.java`
