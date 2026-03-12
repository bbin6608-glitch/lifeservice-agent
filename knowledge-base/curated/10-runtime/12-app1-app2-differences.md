---
id: kb-runtime-app-differences
title: app1 与 app2 差异
category: runtime
tags: [app1, app2, canal, rabbitmq]
usage_scope: [qa, ptest, troubleshooting]
source_files: [application-app1.yaml, application-app2.yaml, application.yaml]
---

# app1 与 app2 差异

## app1
- 端口：8081
- 队列：`cache.invalidate.queue.app1`
- Canal：开启

## app2
- 端口：8082
- 队列：`cache.invalidate.queue.app2`
- Canal：关闭

## 诊断意义
双实例表现不一致时，优先检查队列配置和 Canal 开关是否符合预期。
