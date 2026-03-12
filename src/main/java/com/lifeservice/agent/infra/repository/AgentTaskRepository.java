package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.AgentTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentTaskRepository extends JpaRepository<AgentTaskEntity, String> {
}
