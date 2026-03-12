---
id: kb-source-user-service
title: UserServiceImpl 源码导读
category: source-guide
tags:
  - login
  - user
  - redis
  - token
  - sign
usage_scope:
  - qa
  - ptest
source_files:
  - UserServiceImpl.java
confidence: high
---

# UserServiceImpl 源码导读

## 一句话结论
`UserServiceImpl` 负责验证码发送、登录/注册、退出登录和签到统计，用户态核心存储在 Redis 中。fileciteturn20file7

## 背景/定位
该类既承担登录入口职责，也承担签到位图逻辑，是典型的“用户态 + Redis”服务实现。fileciteturn20file7

## 关键事实
1. `sendCode(String phone)` 会校验手机号格式，然后把 6 位验证码写入 Redis，key 为 `LOGIN_CODE_KEY + phone`。fileciteturn20file7
2. `login(LoginFormDTO loginForm)` 会比对 Redis 中的验证码，若用户不存在则自动注册。fileciteturn20file7
3. 登录成功后生成 UUID token，把 `UserDTO` 转成 Map 存入 Redis Hash，key 为 `LOGIN_USER_KEY + token`，并设置 TTL。fileciteturn20file7
4. `logout(HttpServletRequest request)` 直接删除 `LOGIN_USER_KEY + token`。fileciteturn20file7
5. `sign()` 使用 `setBit()` 做签到位图写入；`signCount()` 使用 `bitField()` 统计连续签到天数。fileciteturn20file7

## 关键类 / 接口 / 配置
- 类：`UserServiceImpl`
- DTO：`LoginFormDTO`、`UserDTO`
- Redis 键：
  - `LOGIN_CODE_KEY`
  - `LOGIN_USER_KEY`
  - `USER_SIGN_KEY`。fileciteturn20file7

## 链路说明 / 机制说明
### 登录链路
1. 发送验证码 → Redis 存储验证码。
2. 登录时校验验证码。
3. 不存在用户则创建用户。
4. 生成 token。
5. 将用户信息写入 Redis Hash 并设置过期时间。fileciteturn20file7

### 签到链路
1. 使用用户 ID 和月份拼接签到 key。
2. `setBit(dayOfMonth - 1, true)` 记录当天签到。
3. `bitField()` 读取截至当天的位串。
4. 通过右移统计连续 1 的数量。fileciteturn20file7

## 对问答 agent 的价值
适合回答：
- 登录态保存在什么地方
- token 怎么生成和存储
- 签到为什么适合用位图

## 对压测 agent 的价值
适合支撑：
- `/user/code`、`/user/login` 为什么是 Redis 写入型接口
- 登录压测时为什么要关注验证码与 token 写入压力

## 已确认事实
- token 使用 `UUID.randomUUID().toString(true)` 生成。fileciteturn20file7
- 登录态使用 Redis Hash，而不是普通字符串。fileciteturn20file7
- 签到统计基于 Redis 位图。fileciteturn20file7

## 合理推断
- 登录态 TTL 的刷新应由拦截器而非本类完成；当前材料中该逻辑未出现在此类源码里，但第一阶段知识库已有 `RefreshTokenInterceptor` 使用点记录。
- `/user/code` 的真实性能瓶颈通常比 `/user/login` 低，但仍可能受 Redis 和短信发送逻辑影响；本次源码未包含真实短信网关调用。

## 来源依据
- `UserServiceImpl.java`
