package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.ChunkMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChunkMetadataRepository extends JpaRepository<ChunkMetadataEntity, String> {
    Optional<ChunkMetadataEntity> findByChunkId(String chunkId);
    void deleteByChunkId(String chunkId);
}
