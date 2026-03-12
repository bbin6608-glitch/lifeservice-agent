package com.lifeservice.agent.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "knowledge_gap_events")
public class KnowledgeGapEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long gapId;
    private String eventType; // DETECTION, CANDIDATE_GEN, RESOLUTION
    private String message;
    private LocalDateTime timestamp;
}
