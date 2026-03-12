package com.lifeservice.agent.service;

import com.lifeservice.agent.dto.agent.AgentChatResponse;
import com.lifeservice.agent.dto.qa.QaAskResponse;
import com.lifeservice.agent.dto.ptest.ResultAnalyzeResponse;
import com.lifeservice.agent.mcp.handler.McpToolHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentGatewayService {

    private final ChatClient chatClient;
    private final McpToolHandler mcpToolHandler;
    
    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    private static final String INTENT_PROMPT = """
            你是一个意图识别专家。请判断用户输入的是寒暄闲聊(CHAT)还是具体操作任务(TASK)。
            
            - CHAT: 你好、在吗、谢谢、你是谁、你能做什么等。
            - TASK: 查源码、问原理、同步库、做压测、分析报告等。
            
            返回格式："intentType": "CHAT" 或 "TASK"
            """;

    private static final String TOOL_PROMPT = """
            你是 Lifeservice Agent 的工具选择助手。
            
            你的任务是：针对 TASK 类型输入，在下面 5 个工具中选择最合适的一个，并补全必要参数。
            
            可选工具：
            
            1. qa_ask
            适用场景：
            - 项目知识问答
            - 架构原理咨询
            - 源码实现说明
            - 链路分析说明
            参数要求：
            - question: 用户原始问题
            - mode: 固定填 rag
            
            2. kb_sync_db
            适用场景：
            - 用户要求同步知识库到数据库
            - 用户要求同步知识索引
            参数要求：
            - 无参数
            
            3. kb_rebuild
            适用场景：
            - 用户要求重建知识库
            - 用户要求重新扫描源码
            - 用户要求重新生成知识碎片
            参数要求：
            - 无参数
            
            4. ptest_start_task
            适用场景：
            - 用户要求启动压测任务
            - 用户要求开始压测
            - 用户要求针对某个接口发起压测流程
            参数要求：
            - endpoint: 从用户输入中提取接口路径，例如 /voucher-order/seckill/11 或 /shop/1
            
            5. ptest_complete_task
            适用场景：
            - 用户要求提交压测结果
            - 用户要求完成压测任务
            - 用户要求基于 taskId 和 reportPath 完成结果分析
            参数要求：
            - taskId
            - reportPath
            
            输出要求：
            1. 只能选择一个工具
            2. 只能输出一行 JSON
            3. 不要输出解释
            4. 不要输出 markdown 代码块
            5. 不要输出额外文本
            
            返回字段：
            - selectedTool: 工具名
            - toolArguments: 参数对象
            
            补充规则：
            - 普通项目问答，优先选择 qa_ask
            - 如果无法明确判断，也优先选择 qa_ask
            - qa_ask 的 mode 固定为 rag
            """;

    public record IntentAnalysis(String intentType, String reason) {}
    public record ToolDecision(String selectedTool, Map<String, Object> toolArguments) {}

    public AgentChatResponse chat(String input) {
        if (input == null || input.trim().isEmpty()) {
            return AgentChatResponse.builder().input(input).finalAnswer("你好！有什么我可以帮你的吗？").build();
        }

        log.info("Agent Gateway analysis for: {}", input);

        // 第 1 阶段：意图判断
        IntentAnalysis intent = analyzeIntent(input);
        
        if ("CHAT".equalsIgnoreCase(intent.intentType())) {
            log.info("Detected CHAT intent. Reason: {}", intent.reason());
            return AgentChatResponse.builder()
                    .input(input)
                    .selectedTool("CHAT_FALLBACK")
                    .finalAnswer("你好！我是 Lifeservice 智能助手。我可以帮助你回答项目源码与架构问题、管理知识库（同步/重建），以及协助你启动压测任务并分析性能瓶颈。请问你想了解哪方面的内容？")
                    .build();
        }

        // 第 2 阶段：工具选择
        ToolDecision decision = decideToolWithLLM(input);
        String toolName = decision.selectedTool();
        Map<String, Object> toolArgs = decision.toolArguments() != null ? decision.toolArguments() : new HashMap<>();

        // 第 3 阶段：执行 MCP
        Object rawResult;
        try {
            rawResult = mcpToolHandler.handle(toolName, toolArgs);
        } catch (Exception e) {
            log.error("MCP call failed: {}", e.getMessage());
            return AgentChatResponse.builder().input(input).selectedTool(toolName).finalAnswer("执行任务时发生内部错误: " + e.getMessage()).build();
        }

        return AgentChatResponse.builder()
                .input(input)
                .selectedTool(toolName)
                .toolArguments(toolArgs)
                .rawToolResult(rawResult)
                .finalAnswer(formatFinalAnswer(toolName, rawResult))
                .build();
    }

    private IntentAnalysis analyzeIntent(String input) {
        try {
            return chatClient.prompt()
                    .user(u -> u.text(INTENT_PROMPT).param("input", input))
                    .call()
                    .entity(IntentAnalysis.class);
        } catch (Exception e) {
            log.warn("Intent analysis failed, assuming TASK: {}", e.getMessage());
            return new IntentAnalysis("TASK", "Fallback due to error");
        }
    }

    private ToolDecision decideToolWithLLM(String input) {
        try {
            return chatClient.prompt()
                    .user(u -> u.text(TOOL_PROMPT).param("input", input))
                    .call()
                    .entity(ToolDecision.class);
        } catch (Exception e) {
            log.warn("Tool decision failed, fallback to qa_ask: {}", e.getMessage());
            return new ToolDecision("qa_ask", Map.of("question", input, "mode", "rag"));
        }
    }

    private String formatFinalAnswer(String toolName, Object result) {
        if (result == null) return "操作已完成。";
        try {
            return switch (toolName) {
                case "qa_ask" -> ((QaAskResponse) result).getAnswer();
                case "kb_sync_db", "kb_rebuild" -> "后台知识库操作执行成功。";
                case "ptest_start_task" -> "已为你启动压测流，请查看本地生成的 JMX 脚本。";
                case "ptest_complete_task" -> "压测分析已完成，最终评定结果为: " + ((ResultAnalyzeResponse)result).getAssessment();
                default -> "工具调用已返回结果。";
            };
        } catch (Exception e) { return "执行成功，正在解析返回结果。"; }
    }
}
