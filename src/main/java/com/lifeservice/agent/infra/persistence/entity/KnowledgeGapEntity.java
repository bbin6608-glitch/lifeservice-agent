package com.lifeservice.agent.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "knowledge_gaps")
public class KnowledgeGapEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String question;
    
    private String gapType; // MISSING_DOC, WEAK_REFERENCE, UNCERTAIN_HIT
    private String missingKnowledgeType; // IMPLEMENTATION, CODE, RUNTIME
    
    @Column(columnDefinition = "TEXT")
    private String suggestedSourceFiles;
    private String suggestedDocType;
    private String suggestedTitle;
    
    private String status; // OPEN, CANDIDATE_CREATED, RESOLVED, IGNORED
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
