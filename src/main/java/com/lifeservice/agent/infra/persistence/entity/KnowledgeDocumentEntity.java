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
@Table(name = "knowledge_documents")
public class KnowledgeDocumentEntity {

    @Id
    private String docId;

    private String title;

    private String category;

    @Column(columnDefinition = "TEXT")
    private String usageScope;

    @Column(columnDefinition = "TEXT")
    private String sourceFiles;

    private String version;

    private String contentHash;

    private Integer priority;

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
