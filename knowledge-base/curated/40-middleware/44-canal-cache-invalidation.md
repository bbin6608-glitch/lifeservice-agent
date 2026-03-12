---
id: kb-middleware-canal
title: Canal 与缓存失效专题
category: middleware
tags: [canal, cache-invalidation, mq]
usage_scope: [qa, ptest, troubleshooting]
source_files: [application.yaml, application-app1.yaml, application-app2.yaml, middleware_usage.txt]
---

# Canal 与缓存失效专题

## 关键事实
- `ShopCanalListener` 受 `app.canal.enabled` 控制
- 监听目标：`lifeservice.tb_shop`
- `app1` 开启 Canal，`app2` 关闭 Canal

## 设计意义
该设计通常用于由单实例承担 binlog 监听职责，再结合消息广播处理多实例缓存一致性。
