package com.lifeservice.agent.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "qa_memory_records")
public class QaMemoryRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    
    @Column(columnDefinition = "TEXT")
    private String question;
    
    @Column(columnDefinition = "TEXT")
    private String normalizedQuestion;
    
    private String answerMode; // local, rag
    private Boolean degradedToLocal;
    
    @Column(columnDefinition = "TEXT")
    private String answerSummary;
    
    @Column(columnDefinition = "TEXT")
    private String topDocIds;
    
    @Column(columnDefinition = "TEXT")
    private String topChunkIds;
    
    private Integer citationsCount;
    private Boolean weakCitations;
    private Boolean insufficientKnowledge;
    
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}
