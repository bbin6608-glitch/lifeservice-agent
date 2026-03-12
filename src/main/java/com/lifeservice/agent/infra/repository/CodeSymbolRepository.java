package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.CodeSymbolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeSymbolRepository extends JpaRepository<CodeSymbolEntity, Long> {
}
