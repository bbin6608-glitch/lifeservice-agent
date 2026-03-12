package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.KnowledgeChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunkEntity, String> {
    Optional<KnowledgeChunkEntity> findByChunkId(String chunkId);
    List<KnowledgeChunkEntity> findByDocId(String docId);
    void deleteByChunkId(String chunkId);
    void deleteByDocId(String docId);
}
