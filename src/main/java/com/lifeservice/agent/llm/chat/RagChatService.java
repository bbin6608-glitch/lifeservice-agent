package com.lifeservice.agent.llm.chat;

import com.lifeservice.agent.knowledge.model.SearchableChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatService {

    private final ChatClient chatClient;
    
    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    private static final String PROMPT_TEMPLATE = """
        你是一个专门负责 local-lifeservice 项目的资深架构师和压测专家。
        
        请根据以下提供的【参考材料】回答用户的【问题】。
        
        ### 约束规则：
        1. 只能基于提供的【参考材料】回答。
        2. 如果材料中没有相关信息，必须明确说明信息不足，不允许编造。
        3. 严禁提及任何参考材料之外的项目细节。
        4. 回答必须严格按照以下结构输出：
        
        结论：
        (一句话总结回答)
        
        已确认事实：
        - (必须且只能来自材料中的确切事实点，每一条都应尽量直接对应到材料内容)
        - ...
        
        合理推断：
        - (基于现有材料推断出的结论，最多 2 条，必须标注“基于现有材料推断”)
        - ...
        
        信息不足：
        - (优先指出哪些类名、方法名、链路细节或配置没有在参考材料里直接出现)
        - (若无明显缺失则写“暂无明显缺失”)
        
        ### 注意事项：
        - “已确认事实”应与参考材料高度对齐，方便用户进行原文追溯。
        - 不要将推断内容混入“已确认事实”。
        
        ### 参考材料：
        {context}
        
        ### 问题：
        {question}
        """;

    public String call(String question, List<SearchableChunk> chunks) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "错误：DASHSCOPE_API_KEY 未配置，无法使用 RAG 模式。";
        }

        String context = chunks.stream()
                .map(c -> String.format("[来源:%s, 标题:%s] %s", c.getDocId(), c.getTitle(), c.getContent()))
                .collect(Collectors.joining("\n\n"));

        try {
            String answer = chatClient.prompt()
                    .user(u -> u.text(PROMPT_TEMPLATE)
                            .param("context", context)
                            .param("question", question))
                    .call()
                    .content();
            
            return answer + "\n\n该回答基于本地知识检索与通义模型组织生成。";
        } catch (Exception e) {
            log.error("Failed to call LLM via OpenAI compatible API: {}", e.getMessage(), e);
            throw new RuntimeException("LLM 调用失败: " + e.getMessage());
        }
    }
}
