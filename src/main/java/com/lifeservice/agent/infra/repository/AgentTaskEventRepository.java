package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.AgentTaskEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentTaskEventRepository extends JpaRepository<AgentTaskEventEntity, Long> {
    List<AgentTaskEventEntity> findByTaskId(String taskId);
}
