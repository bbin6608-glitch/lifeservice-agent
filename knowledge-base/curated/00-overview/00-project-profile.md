---
id: kb-overview-project-profile
title: 项目总览
category: overview
tags: [spring-boot, redis, rabbitmq, canal, seckill]
usage_scope: [qa, ptest]
source_files: [project_summary.md, bootstrap.txt, pom.xml]
---

# 项目总览

## 一句话结论
local-lifeservice 是一个基于 Spring Boot 2.7.18 和 Java 17 的单体后端项目，围绕登录鉴权、商铺缓存、优惠券与秒杀下单等核心业务展开，并使用 Redis、RabbitMQ、Lua、Canal 等组件支撑高并发与缓存一致性。

## 技术基线
- Spring Boot 2.7.18
- Java 17
- MyBatis-Plus + MySQL
- Redis + StringRedisTemplate
- RabbitMQ
- Lua 脚本
- Canal
- Caffeine

## 模块概览
主要代码位于 `src/main/java/com/lifeservice` 下，按 controller、service、mapper、config、cache、utils 等分层组织。

## 核心链路
- 登录鉴权链路
- 商铺缓存查询链路
- 秒杀下单链路

## 适用场景
- 项目问答
- 接口定位
- 压测对象识别
- 排障入口文档
