package com.lifeservice.agent.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeChunk {
    private String chunkId;
    private String docId;
    private String title;
    private String section;
    private String content;
    private String category;
    
    // 新增字段
    private String knowledgeType;
    private String sourceMode;
    private Double confidenceLevel;
    
    private ChunkMetadata metadata;
}
