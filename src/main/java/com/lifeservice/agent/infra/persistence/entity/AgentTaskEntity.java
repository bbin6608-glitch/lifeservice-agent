package com.lifeservice.agent.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agent_tasks")
public class AgentTaskEntity {
    @Id
    private String taskId;
    private String type; // PTEST
    private String status; // PENDING, RUNNING, WAITING_CONFIRMATION, SUCCESS, FAILED
    private String endpoint;
    private String currentStep;
    
    @Column(columnDefinition = "TEXT")
    private String inputParams;
    
    @Column(columnDefinition = "TEXT")
    private String resultSummary;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
