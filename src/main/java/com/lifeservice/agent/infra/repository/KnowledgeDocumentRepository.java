package com.lifeservice.agent.infra.repository;

import com.lifeservice.agent.infra.persistence.entity.KnowledgeDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocumentEntity, String> {
    Optional<KnowledgeDocumentEntity> findByDocId(String docId);
    List<KnowledgeDocumentEntity> findByDocIdIn(List<String> docIds);
    void deleteByDocId(String docId);
}
