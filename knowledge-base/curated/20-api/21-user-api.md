---
id: kb-api-user
title: 用户接口
category: api
tags: [user, login, auth]
usage_scope: [qa, ptest]
source_files: [api_mappings.txt, redis_usage.txt]
---

# 用户接口

## 主要接口
- `POST /user/code`
- `POST /user/login`
- `POST /user/logout`
- `GET /user/me`
- `GET /user/info/{id}`
- `GET /user/{id}`
- `POST /user/sign`
- `GET /user/sign/count`

## 相关说明
登录验证码、Token 和签到相关状态都与 Redis 使用相关。
