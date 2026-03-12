package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.RetrievalLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetrievalLogRepository extends JpaRepository<RetrievalLogEntity, Long> {
}
