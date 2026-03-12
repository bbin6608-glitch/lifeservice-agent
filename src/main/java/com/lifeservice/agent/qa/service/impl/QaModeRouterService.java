package com.lifeservice.agent.qa.service.impl;

import com.lifeservice.agent.dto.qa.QaAskRequest;
import com.lifeservice.agent.dto.qa.QaAskResponse;
import com.lifeservice.agent.qa.service.QaAgentService;
import com.lifeservice.agent.qa.service.QaKnowledgeGapService;
import com.lifeservice.agent.qa.service.QaMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

@Slf4j
@Primary
@Service
public class QaModeRouterService implements QaAgentService {

    private final QaAgentService localAgent;
    private final QaAgentService ragAgent;
    private final QaMemoryService memoryService;
    private final QaKnowledgeGapService gapService;

    private static final Set<String> CHAT_KEYWORDS = Set.of("你好", "hi", "hello", "在吗", "谢谢", "早上好", "下午好", "晚上好");

    public QaModeRouterService(
            @Qualifier("localModeQaAgentService") QaAgentService localAgent,
            @Qualifier("ragModeQaAgentService") QaAgentService ragAgent,
            QaMemoryService memoryService,
            QaKnowledgeGapService gapService) {
        this.localAgent = localAgent;
        this.ragAgent = ragAgent;
        this.memoryService = memoryService;
        this.gapService = gapService;
    }

    @Override
    public QaAskResponse ask(QaAskRequest request) {
        String question = request.getQuestion() != null ? request.getQuestion().trim().toLowerCase() : "";
        
        // 1. 意图判断：闲聊/寒暄类处理
        if (isChatIntent(question)) {
            QaAskResponse chatResponse = QaAskResponse.builder()
                    .answer(generateChatReply(question))
                    .hits(new ArrayList<>())
                    .citations(new ArrayList<>())
                    .answerMode("chat")
                    .degradedToLocal(false)
                    .build();
            memoryService.recordMemory(request.getQuestion(), chatResponse);
            return chatResponse;
        }

        // 2. 正常问答逻辑
        QaAskResponse response;
        if ("rag".equalsIgnoreCase(request.getMode())) {
            response = ragAgent.ask(request);
        } else {
            response = localAgent.ask(request);
        }

        memoryService.recordMemory(request.getQuestion(), response);
        gapService.analyzeAndRecordGap(request.getQuestion(), response);

        return response;
    }

    private boolean isChatIntent(String question) {
        if (question.length() > 10) return false; // 太长的一般不是简单寒暄
        for (String kw : CHAT_KEYWORDS) {
            if (question.contains(kw)) return true;
        }
        return false;
    }

    private String generateChatReply(String question) {
        if (question.contains("谢谢")) return "不客气，很高兴能帮到你！";
        if (question.contains("早上好")) return "早上好！今天有什么我可以帮你的吗？";
        return "你好！我是 Lifeservice 智能助手，你可以问我关于项目架构、源码实现或压测方案的问题。";
    }
}
