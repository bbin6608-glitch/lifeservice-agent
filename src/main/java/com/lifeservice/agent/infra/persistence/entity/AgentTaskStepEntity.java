package com.lifeservice.agent.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agent_task_steps")
public class AgentTaskStepEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskId;
    private String stepName; // PLAN, JMX, EXECUTE, ANALYZE
    private String status;
    private Integer ordinal;
    
    @Column(columnDefinition = "TEXT")
    private String outputData;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
