---
id: kb-flow-login-auth
title: 登录鉴权链路
category: core-flow
tags: [login, auth, redis, token]
usage_scope: [qa, ptest]
source_files: [project_summary.md, redis_usage.txt]
---

# 登录鉴权链路

## 执行步骤
1. `UserServiceImpl` 将验证码写入 Redis。
2. 登录时校验 Redis 中的验证码。
3. 登录成功后将用户信息写入 Redis Hash，并设置过期时间。
4. `RefreshTokenInterceptor` 从 Redis 恢复用户态并刷新 TTL。
5. 用户上下文通过 `UserHolder` 维持。

## 涉及组件
- Redis
- MVC 拦截器
- ThreadLocal
