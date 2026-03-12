---
id: kb-method-userservice-login
title: UserServiceImpl.login() 方法卡片
category: method-card
tags:
  - login
  - method
  - redis
  - token
usage_scope:
  - qa
  - ptest
source_files:
  - UserServiceImpl.java
confidence: high
---

# UserServiceImpl.login() 方法卡片

## 一句话结论
该方法完成验证码校验、自动注册、token 生成和 Redis 登录态写入，是登录链路最关键的方法。fileciteturn20file7

## 背景/定位
方法签名：`public Result login(LoginFormDTO loginForm)`。fileciteturn20file7

## 关键事实
- 先校验手机号。
- 再取 Redis 中验证码比对。
- 用户不存在则 `createUserWithPhone(phone)` 自动注册。
- token 为 UUID。
- Redis 存储形式为 Hash。fileciteturn20file7

## 关键类 / 接口 / 配置
- `LoginFormDTO`
- `UserDTO`
- `StringRedisTemplate`
- `LOGIN_USER_KEY`
- `LOGIN_USER_TTL`。fileciteturn20file7

## 链路说明 / 机制说明
登录不是把整个 `User` 实体直接存入 Redis，而是把 `UserDTO` 转成字符串 Map 放入 Hash，并设置过期时间。fileciteturn20file7

## 对问答 agent 的价值
适合解释“登录态为什么存在 Redis Hash”。

## 对压测 agent 的价值
适合说明登录压测为什么要关注 Redis 写入压力和 token 存储。

## 已确认事实
- 该方法返回 token。fileciteturn20file7

## 合理推断
- 登录态刷新逻辑不在本方法中，应由拦截器承担。

## 来源依据
- `UserServiceImpl.java`
