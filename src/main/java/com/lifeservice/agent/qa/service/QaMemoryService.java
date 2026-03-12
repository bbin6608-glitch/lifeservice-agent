package com.lifeservice.agent.qa.service;

import com.lifeservice.agent.dto.qa.QaAskResponse;
import com.lifeservice.agent.infra.persistence.entity.QaMemoryRecordEntity;
import com.lifeservice.agent.infra.repository.QaMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QaMemoryService {

    private final QaMemoryRepository memoryRepository;

    @Transactional
    public void recordMemory(String question, QaAskResponse response) {
        try {
            // 判定是否为弱引用或材料不足
            boolean isWeak = response.getCitations() == null || response.getCitations().size() < 2;
            boolean isInsufficient = response.getAnswer() != null && 
                    (response.getAnswer().contains("未检索到") || response.getAnswer().contains("材料不足"));

            QaMemoryRecordEntity record = QaMemoryRecordEntity.builder()
                    .question(question)
                    .answerMode(response.getAnswerMode())
                    .degradedToLocal(response.getDegradedToLocal())
                    .answerSummary(response.getAnswer() != null ? 
                            response.getAnswer().substring(0, Math.min(200, response.getAnswer().length())) : "")
                    .citationsCount(response.getCitations() != null ? response.getCitations().size() : 0)
                    .topDocIds(response.getCitations() != null ? 
                            response.getCitations().stream().map(c -> c.getDocId()).distinct().collect(Collectors.joining(",")) : "")
                    .weakCitations(isWeak)
                    .insufficientKnowledge(isInsufficient)
                    .build();

            memoryRepository.save(record);
            log.info("QA memory recorded for question: {}", question);
        } catch (Exception e) {
            log.error("Failed to record QA memory: {}", e.getMessage());
        }
    }
}
