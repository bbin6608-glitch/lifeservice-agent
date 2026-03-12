---
id: kb-source-threadpool-canal
title: ThreadPoolConfig 与 ShopCanalListener 源码导读
category: source-guide
tags:
  - threadpool
  - canal
  - cache-invalidation
  - mq
usage_scope:
  - qa
  - ptest
source_files:
  - ThreadPoolConfig.java
  - ShopCanalListener.java
  - RabbitMqConfig.java
confidence: high
---

# ThreadPoolConfig 与 ShopCanalListener 源码导读

## 一句话结论
当前项目把“缓存重建”和“Canal 监听”拆成两套独立线程池；`ShopCanalListener` 仅在启用时运行，监听 `lifeservice.tb_shop` 的 binlog，并通过 MQ 广播商铺缓存失效事件。fileciteturn20file5turn20file2

## 背景/定位
这是多实例缓存一致性链路的源码基础：
- `ThreadPoolConfig` 提供执行资源
- `ShopCanalListener` 提供变更监听
- `RabbitMqConfig` 提供广播通道。fileciteturn20file5turn20file2turn20file3

## 关键事实
1. `cacheRebuildExecutor()` 线程池参数为 core 4 / max 8 / queue 100。fileciteturn20file5
2. `canalExecutorService()` 是单线程顺序消费，线程名格式 `canal-shop-worker-x`，daemon=true。fileciteturn20file5
3. `ShopCanalListener` 通过 `@Value("${app.canal.enabled:true}")` 控制是否启用。fileciteturn20file2
4. 监听目标严格限定为 schema `lifeservice`、table `tb_shop`。fileciteturn20file2
5. 仅处理 INSERT / UPDATE / DELETE 三类 DML 事件。fileciteturn20file2
6. 监听到 shop id 后，会构造 `CacheInvalidateMessage` 并调用 `cacheInvalidationPublisher.publish(message)`。fileciteturn20file2

## 关键类 / 接口 / 配置
- `ThreadPoolConfig`
- `ShopCanalListener`
- `CanalConnector`
- `ExecutorService`
- `CacheInvalidationPublisher`
- `CacheInvalidateMessage`。fileciteturn20file5turn20file2

## 链路说明 / 机制说明
### Canal 监听启动
1. 应用启动时执行 `@PostConstruct start()`
2. 若 `enabled=false`，直接跳过。
3. 创建 `CanalConnector`
4. 提交 `worker()` 到 `canalExecutorService`。fileciteturn20file2

### worker 主循环
1. 连接 Canal
2. 订阅 `lifeservice\.tb_shop`
3. `getWithoutAck(batchSize)` 拉取批次
4. 解析 `RowChange`
5. 提取变更 ID
6. 发布缓存失效消息
7. 成功则 `ack`，异常则 `rollback`。fileciteturn20file2

## 对问答 agent 的价值
适合回答：
- 商铺缓存为什么能跨实例失效
- Canal 监听是怎么跑起来的
- 为什么 app1/app2 行为不同

## 对压测 agent 的价值
适合支撑：
- 多实例压测时为什么要关注 Canal 开关差异
- 为什么商铺更新与查询混压时可能出现缓存同步问题

## 已确认事实
- Canal 监听是单线程串行消费。fileciteturn20file5
- 监听器不直接删缓存，而是发布缓存失效消息。fileciteturn20file2

## 合理推断
- 由于监听器聚焦 `tb_shop`，当前 Canal 方案主要服务商铺缓存一致性，而不是全局所有业务对象。
- 在 `app.canal.enabled=false` 的实例上，仍可能通过 MQ 接收其他实例发出的缓存失效广播来完成本地缓存同步；这需要结合完整消费端源码进一步确认。

## 来源依据
- `ThreadPoolConfig.java`
- `ShopCanalListener.java`
- `RabbitMqConfig.java`
