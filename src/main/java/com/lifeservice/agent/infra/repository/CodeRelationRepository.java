package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.CodeRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRelationRepository extends JpaRepository<CodeRelationEntity, Long> {
}
