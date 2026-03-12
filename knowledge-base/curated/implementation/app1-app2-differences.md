# App1 与 App2 核心运行差异总结

## 一句话结论
App1 是标准单机业务节点，而 App2 是通过 Profile 激活的模拟集群节点或特定压测节点，二者在端口、缓存策略及中间件连接上存在显著差异。

## 背景
为了在本地单机环境模拟高并发和分布式场景，本项目通过 Spring Boot Profile 机制派生出 App1 和 App2 两个运行实例。

## App1 特点
- **端口**: 8081
- **定位**: 主业务逻辑节点。
- **缓存**: 默认开启本地二级缓存（Caffeine）。
- **任务**: 承担主要的 API 请求处理和 MQ 消息下发。

## App2 特点
- **端口**: 8082
- **定位**: 备用节点或特定压测目标节点。
- **缓存**: 本地缓存容量限制更小，更依赖远端 Redis。
- **任务**: 常用于测试负载均衡下的数据一致性。

## 差异总结
| 特性 | App1 | App2 | 备注 |
| :--- | :--- | :--- | :--- |
| Server Port | 8081 | 8082 | 避免单机冲突 |
| Profile | default / app1 | app2 | 决定配置加载顺序 |
| Redis Namespace | hmdp: | hmdp: | 共享数据空间 |
| 日志级别 | INFO | WARN | App2 通常减小日志量 |

## 风险点
1. **端口冲突**: 同时启动时需确保配置文件中的 `server.port` 被正确覆盖。
2. **状态不同步**: 虽共享 Redis，但本地缓存（Caffeine）在两台 App 间不互通，可能导致短时间的数据可见性差异。

## 验证方式
- 访问 `http://localhost:8081/health` 检查 App1 状态。
- 访问 `http://localhost:8082/health` 检查 App2 状态。

## 相关源码
- `application-app1.yaml`
- `application-app2.yaml`
- `VoucherOrderServiceImpl.java`
- `RefreshTokenInterceptor.java`
