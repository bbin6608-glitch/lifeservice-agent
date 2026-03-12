package com.lifeservice.agent.knowledge.store;

import com.lifeservice.agent.infra.persistence.entity.ChunkMetadataEntity;
import com.lifeservice.agent.infra.persistence.entity.KnowledgeChunkEntity;
import com.lifeservice.agent.infra.persistence.entity.KnowledgeDocumentEntity;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KnowledgePersistenceMapper {

    public KnowledgeChunkEntity toChunkEntity(SearchableChunk chunk) {
        if (chunk == null) return null;
        return KnowledgeChunkEntity.builder()
                .chunkId(chunk.getChunkId())
                .docId(chunk.getDocId())
                .title(chunk.getTitle())
                .section(chunk.getSection())
                .category(chunk.getCategory())
                .content(chunk.getContent())
                .priority(chunk.getPriority())
                .knowledgeType(chunk.getKnowledgeType())
                .sourceMode(chunk.getSourceMode())
                .confidenceLevel(chunk.getConfidenceLevel())
                .build();
    }

    public ChunkMetadataEntity toMetadataEntity(SearchableChunk chunk) {
        if (chunk == null) return null;
        return ChunkMetadataEntity.builder()
                .chunkId(chunk.getChunkId())
                .tags(toListString(chunk.getTags()))
                .scenarioTags(toListString(chunk.getScenarioTags()))
                .usageScope(toListString(chunk.getUsageScope()))
                .endpoints(toListString(chunk.getEndpoints()))
                .middleware(toListString(chunk.getMiddleware()))
                .sourceFiles(toListString(chunk.getSourceFiles()))
                .build();
    }

    public KnowledgeDocumentEntity toDocumentEntity(Map<String, Object> catalogItem) {
        if (catalogItem == null) return null;
        return KnowledgeDocumentEntity.builder()
                .docId((String) catalogItem.get("id"))
                .title((String) catalogItem.get("title"))
                .category((String) catalogItem.get("category"))
                .usageScope(toListString((List<String>) catalogItem.get("usage_scope")))
                .sourceFiles(toListString((List<String>) catalogItem.get("source_files")))
                .priority(1)
                .build();
    }

    private String toListString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(",", list);
    }
}
