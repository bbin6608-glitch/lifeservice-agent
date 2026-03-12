本地生活服务垂直智能Agent项目（Java/AI Agent）

项目介绍：面向本地生活服务项目的垂直智能Agent系统，围绕项目知识问答、知识库管理与压测任务三类场景，构建集RAG检索、MCP工具调用、任务工作流于一体的Agent

技术栈：Spring Boot、Spring AI、PostgreSQL、Lucene、Embedding、RAG、MCP、React、JMeter

主要工作：

基于Hybrid RAG构建项目知识检索链路，融合BM25、向量检索与多维Rerank，结合知识类型、来源模式、置信度与场景标签提升复杂问题下的召回质量与可解释性

设计并实现源码知识+实施经验双流知识库体系，打通raw->chunks->DB->Lucene->Vector全流程，支持FAQ、源码导读、方法卡片、压测经验等多类型知识沉淀

构建执行型Ptest Agent，将压测方案生成、JMX脚本生成、结果分析与报告输出串联为任务工作流，支持任务状态、步骤追踪与事件记录，提升压测流程自动化程度

基于MCP对问答、知识库同步/重建、压测任务启动/完成等能力进行标准化封装，并构建最小Agent Gateway，支持上层模型通过统一入口完成意图识别、工具选择与能力调用