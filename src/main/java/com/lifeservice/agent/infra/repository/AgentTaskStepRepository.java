package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.AgentTaskStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentTaskStepRepository extends JpaRepository<AgentTaskStepEntity, Long> {
    List<AgentTaskStepEntity> findByTaskId(String taskId);
}
