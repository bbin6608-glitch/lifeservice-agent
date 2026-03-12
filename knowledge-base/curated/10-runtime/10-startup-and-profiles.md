---
id: kb-runtime-startup-profiles
title: 启动方式与 Profiles
category: runtime
tags: [startup, profiles, app1, app2]
usage_scope: [qa, troubleshooting]
source_files: [application.yaml, application-app1.yaml, application-app2.yaml]
---

# 启动方式与 Profiles

## 默认配置
- 默认激活 profile：`app1`
- 默认服务端口：`${PORT:8081}`

## 基础依赖服务
- MySQL：`127.0.0.1:3307/lifeservice`
- Redis：`localhost:6379`
- RabbitMQ：`localhost:5672`
- Canal：`127.0.0.1:11111`

## 运行建议
单实例默认用 `app1` 启动；双实例演示时可同时启动 `app1` 和 `app2`，观察缓存广播与 Canal 行为差异。
