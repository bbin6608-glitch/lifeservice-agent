package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.QaMemoryRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QaMemoryRepository extends JpaRepository<QaMemoryRecordEntity, Long> {
}
