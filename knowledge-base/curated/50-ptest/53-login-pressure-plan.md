---
id: kb-ptest-login-plan
title: 登录接口压测方案
category: ptest
tags: [ptest, login, redis]
usage_scope: [ptest]
source_files: [api_mappings.txt, redis_usage.txt]
---

# 登录接口压测方案

## 目标接口
- `POST /user/code`
- `POST /user/login`

## 压测重点
- 验证码写入 Redis 的稳定性
- Token 写入 Hash 与 TTL 刷新行为
- 登录高峰时 Redis RT
