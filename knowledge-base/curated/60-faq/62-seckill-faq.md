# 秒杀 FAQ

- 秒杀接口的核心入口是什么？
  - `POST /voucher-order/seckill/{id}`。
- 秒杀为什么要用 Lua？
  - 为了把库存校验、一人一单和预扣减做成 Redis 原子操作。
