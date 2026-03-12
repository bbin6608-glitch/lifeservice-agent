package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.KnowledgeGapEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeGapEventRepository extends JpaRepository<KnowledgeGapEventEntity, Long> {
}
