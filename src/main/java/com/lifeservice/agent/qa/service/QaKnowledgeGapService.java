package com.lifeservice.agent.qa.service;

import com.lifeservice.agent.dto.qa.QaAskResponse;
import com.lifeservice.agent.infra.persistence.entity.KnowledgeGapEntity;
import com.lifeservice.agent.infra.persistence.entity.KnowledgeGapEventEntity;
import com.lifeservice.agent.infra.repository.KnowledgeGapEventRepository;
import com.lifeservice.agent.infra.repository.KnowledgeGapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QaKnowledgeGapService {

    private final KnowledgeGapRepository gapRepository;
    private final KnowledgeGapEventRepository eventRepository;

    @Transactional
    public void analyzeAndRecordGap(String question, QaAskResponse response) {
        if (question == null || question.trim().isEmpty()) {
            return;
        }
        
        // 判定规则 1: 显式材料不足
        boolean isInsufficient = response.getAnswer() != null && 
                (response.getAnswer().contains("未检索到") || response.getAnswer().contains("材料不足"));
        
        // 判定规则 2: 引文极弱 (0或1条)
        int citations = response.getCitations() != null ? response.getCitations().size() : 0;
        
        // 判定规则 3: 缺少高置信度人工文档支撑 (只有原始源码命中)
        boolean missingManualDoc = response.getHits() != null && 
                response.getHits().stream().noneMatch(h -> "hybrid".equals(h.getEngine()) && h.getRerankScore() > 0.5);

        if (isInsufficient || citations < 1 || missingManualDoc) {
            recordGap(question, isInsufficient, citations, missingManualDoc);
        }
    }

    private void recordGap(String question, boolean isInsufficient, int citations, boolean missingManualDoc) {
        String gapType = isInsufficient ? "MISSING_DOC" : (citations < 1 ? "WEAK_REFERENCE" : "UNCERTAIN_HIT");
        String reason = String.format("判定依据: 显式不足=%b, 引文数=%d, 缺少精修文档=%b", isInsufficient, citations, missingManualDoc);

        KnowledgeGapEntity gap = KnowledgeGapEntity.builder()
                .question(question)
                .gapType(gapType)
                .status("OPEN")
                .reason(reason)
                .build();

        // 自动推断 Candidate Knowledge Doc 建议
        generateCandidateSuggestions(gap);

        gapRepository.save(gap);
        
        eventRepository.save(KnowledgeGapEventEntity.builder()
                .gapId(gap.getId())
                .eventType("DETECTION")
                .message("检测到知识缺口: " + gapType)
                .timestamp(LocalDateTime.now())
                .build());
        
        log.info("Knowledge Gap identified and recorded for: {}", question);
    }

    private void generateCandidateSuggestions(KnowledgeGapEntity gap) {
        if (gap.getQuestion() == null) return;
        String q = gap.getQuestion().toLowerCase();
        
        if (q.contains("为什么") || q.contains("原因") || q.contains("原理")) {
            gap.setSuggestedDocType("implementation");
            gap.setSuggestedTitle("关于 " + extractSubject(gap.getQuestion()) + " 的设计原理与实施经验");
            gap.setMissingKnowledgeType("IMPLEMENTATION");
        } else if (q.contains("接口") || q.contains("路径") || q.contains("/")) {
            gap.setSuggestedDocType("runtime");
            gap.setSuggestedTitle("接口 " + extractSubject(gap.getQuestion()) + " 的运行时配置与路由说明");
            gap.setMissingKnowledgeType("RUNTIME");
        } else {
            gap.setSuggestedDocType("source-guide");
            gap.setSuggestedTitle(extractSubject(gap.getQuestion()) + " 核心逻辑导读");
            gap.setMissingKnowledgeType("CODE");
        }
    }

    private String extractSubject(String question) {
        if (question == null) return "";
        // 简易主体提取
        return question.replace("针对", "").replace("如何实现", "").replace("是什么", "").trim();
    }
}
