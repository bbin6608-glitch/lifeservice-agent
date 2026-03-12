---
id: kb-middleware-lua
title: Lua 脚本使用专题
category: middleware
tags: [lua, redis, unlock, seckill]
usage_scope: [qa, ptest]
source_files: [lua_refs.txt, resource_files.txt]
---

# Lua 脚本使用专题

## 使用点
- `CacheClient` 使用 `UNLOCK_SCRIPT`
- `VoucherOrderServiceImpl` 使用 `SECKILL_SCRIPT`

## 脚本位置
- `src/main/resources/seckill.lua`

## 作用
- 解锁脚本用于安全释放锁
- 秒杀脚本用于原子校验库存、一人一单和预扣减
