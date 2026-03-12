package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.ChunkEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChunkEmbeddingRepository extends JpaRepository<ChunkEmbeddingEntity, String> {
}
