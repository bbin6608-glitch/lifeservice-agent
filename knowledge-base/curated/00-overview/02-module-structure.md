---
id: kb-overview-module-structure
title: 模块结构与职责
category: overview
tags: [controller, service, mapper, config, cache, utils]
usage_scope: [qa]
source_files: [tree.txt, project_summary.md, configs.txt, services.txt, utils.txt]
---

# 模块结构与职责

## 分层说明
- `controller/`：REST 接口入口
- `service/` 与 `service/impl/`：核心业务逻辑
- `mapper/`：数据库映射层
- `config/`：全局配置、中间件配置、线程池、预热等
- `cache/`：缓存工具、常量、缓存广播与消费
- `utils/`：通用工具类与上下文管理
- `canal/`：数据库变更监听

## 重点实现类
- `VoucherOrderServiceImpl`
- `ShopServiceImpl`
- `UserServiceImpl`
- `VoucherServiceImpl`
- `CacheClient`
- `RedisIdWorker`
- `ShopCanalListener`
