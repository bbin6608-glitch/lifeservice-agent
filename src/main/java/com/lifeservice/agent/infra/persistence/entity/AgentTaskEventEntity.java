package com.lifeservice.agent.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agent_task_events")
public class AgentTaskEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskId;
    private String level; // INFO, WARN, ERROR
    private String eventType;
    private String message;
    private LocalDateTime timestamp;
}
