# 项目结构与核心技术总结 (Project Summary)

## 1. 技术栈 (Tech Stack)
- **核心框架**: Spring Boot
- **持久层框架**: MyBatis-Plus, MySQL
- **缓存与原子操作**: Redis (StringRedisTemplate), Lua 脚本
- **消息队列**: RabbitMQ (用于异步削峰填谷)
- **工具库**: Hutool (JSON/字符串处理), Lombok

## 2. 模块分层 (Module Layers)
该项目采用了经典的单体分层架构，主要分布在 `src/main/java/com/lifeservice` 下：
- **`controller/`**: 暴露外部 RESTful API 接口，负责请求接收和参数校验。
- **`service/` & `service/impl/`**: 存放核心业务逻辑（缓存读写、缓存穿透/击穿防护、分布式锁实现、异步解耦等）。
- **`mapper/`**: MyBatis-Plus 映射层，负责底层数据库交互。
- **`entity/` & `dto/`**: 数据库映射实体类和前后端交互的数据传输对象。
- **`config/`**: 全局配置类（如 Redis序列化、MVC拦截器、RabbitMQ、线程池配置、启动预热配置等）。
- **`cache/`**: Redis 常量与缓存公共相关内容维护。
- **`utils/`**: 公共工具类（如 RedisIdWorker 全局唯一ID生成、UserHolder ThreadLocal 上下文等）。

## 3. 核心业务链路 (Core Business Flow)
- **登录鉴权链路**: 基于 Redis 存储验证码及 Token，通过 Spring MVC 拦截器校验并将用户态存入 ThreadLocal (`UserHolder`)。
- **商铺查询链路**: 先查 Redis 缓存，结合 `缓存空值 (Cache Null)` 防御缓存穿透；引入过期时间 + 逻辑过期控制策略处理缓存击穿问题。
- **高并发秒杀链路 (Seckill)**:
  1. 系统启动时通过 `SeckillStockWarmUpRunner` 将未结束秒杀券库存预热至 Redis。
  2. 用户发起秒杀请求，通过 Redis 执行 `seckill.lua` 脚本（校验库存、一人一单、预扣库存）实现**原子化**的高并发判断。
  3. 脚本校验通过后，使用 `RedisIdWorker` 生成全局唯一订单 ID，并快速返回给用户。
  4. 后台将下单信息封装为 `SeckillOrderMessage` 发送至 RabbitMQ，由 Consumer 异步落库。
  5. 消费者中包含 DB 乐观锁扣减及重复下单兜底校验。

## 4. 中间件使用点汇总 (Middleware Usage)
- **Redis & Lua**: 
  - Token 管理、数据缓存。
  - **Lua 脚本**：`seckill.lua` 专用于解决高并发下查库存与扣库存并发不一致以及一人一单并发安全性问题。
- **RabbitMQ**:
  - 用于秒杀下单的异步削峰，提升响应速度并解耦 Redis 与 DB 持久化。
- **线程池 (ThreadPool)**:
  - 存在于 `ThreadPoolConfig`，可能用于其他无需强一致性的后台异步任务。

## 5. 适合做知识库的重点目录
1. `src/main/java/com/lifeservice/service/impl`: 这里承载了所有的复杂业务逻辑（如缓存双写一致性、异步秒杀）。
2. `src/main/resources/seckill.lua`: 核心并发控制逻辑。
3. `src/main/java/com/lifeservice/config`: 包含各个中间件整合的精华（RabbitMQ 绑定、MVC 拦截器组装、Redis 序列化）。

## 6. 新人应优先阅读的关键类
- **`VoucherOrderServiceImpl.java`**: 该项目的并发精髓，包含了 Lua 脚本执行、MQ 发送、兜底策略和最终事务一致性设计。
- **`VoucherServiceImpl.java`**: 涉及缓存和数据库一致性的处理以及秒杀券的初始化。
- **`SeckillStockWarmUpRunner.java`**: Spring Boot 生命周期扩展的经典用法，理解系统启动时的数据预热流程。
- **`RedisIdWorker.java`**: 基于 Redis 的高并发、高可用全局唯一序列号生成器实现。
- **`RedisConstants.java`**: 项目内所有的 Redis key 字典，是理解缓存结构的总纲。