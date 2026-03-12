# 源码级知识切片策略

## 适用范围
- `70-source-guides`: 类/模块级源码导读
- `71-method-cards`: 关键方法卡片

## 切片规则
1. 以每个 `##` 二级标题作为主要 chunk 边界。
2. 如果文档没有二级标题，则整个正文作为一个 chunk。
3. `70-source-guides` 默认 `priority=8`。
4. `71-method-cards` 默认 `priority=10`。

## 推荐 chunk 大小
- 300 到 900 中文字
- 单个 chunk 保持一个完整语义点，不混入多个主题

## metadata 字段
- `chunkId`
- `docId`
- `title`
- `category`
- `section`
- `tags`
- `usageScope`
- `sourceFiles`
- `endpoints`
- `middleware`
- `priority`

## 检索过滤建议
优先按以下字段过滤或加权：
- `category`: `source-guide`, `method-card`
- `tags`
- `sourceFiles`
- `endpoints`
- `middleware`
- `priority`

## 适用问答
适合回答：
- 某个类在链路里的职责
- 某个方法如何实现
- Redis/Lua/MQ/Canal 在源码里的落点
