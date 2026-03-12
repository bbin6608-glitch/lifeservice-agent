package com.lifeservice.agent.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "knowledge_chunks")
public class KnowledgeChunkEntity {
    @Id
    private String chunkId;
    private String docId;
    private String title;
    private String section;
    private String category;
    
    // 新增字段
    private String knowledgeType;    // CODE, IMPLEMENTATION
    private String sourceMode;       // AUTO, MANUAL
    private Double confidenceLevel;  // 0.0 - 1.0
    
    @Column(columnDefinition = "TEXT")
    private String content;
    private Integer priority;
    private Integer ordinalNo;
    private String contentHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
