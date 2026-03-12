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
@Table(name = "retrieval_logs")
public class RetrievalLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String queryText;

    private String mode;

    private String sceneType;

    @Column(columnDefinition = "TEXT")
    private String recalledChunkIds;

    @Column(columnDefinition = "TEXT")
    private String finalChunkIds;

    private String answerMode;

    private Boolean degradedToLocal;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
