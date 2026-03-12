package com.lifeservice.agent.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {
    private String docId;
    private String title;
    private String category;
    private List<String> tags;
    private List<String> usageScope;
    private List<String> sourceFiles;
    private String content;
    
    // 新增字段
    @Builder.Default
    private String knowledgeType = "IMPLEMENTATION";
    @Builder.Default
    private String sourceMode = "MANUAL";
    @Builder.Default
    private Double confidenceLevel = 1.0;
}
