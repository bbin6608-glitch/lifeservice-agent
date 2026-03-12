package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.KnowledgeGapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeGapRepository extends JpaRepository<KnowledgeGapEntity, Long> {
}
