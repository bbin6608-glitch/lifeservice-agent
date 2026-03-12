package com.lifeservice.agent.knowledge.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lifeservice.agent.config.KnowledgeBaseProperties;
import com.lifeservice.agent.infra.persistence.entity.ChunkMetadataEntity;
import com.lifeservice.agent.infra.persistence.entity.KnowledgeChunkEntity;
import com.lifeservice.agent.infra.persistence.entity.KnowledgeDocumentEntity;
import com.lifeservice.agent.infra.repository.ChunkMetadataRepository;
import com.lifeservice.agent.infra.repository.KnowledgeChunkRepository;
import com.lifeservice.agent.infra.repository.KnowledgeDocumentRepository;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import com.lifeservice.agent.knowledge.retrieve.Bm25SearchService;
import com.lifeservice.agent.knowledge.retrieve.VectorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSyncService {

    private final KnowledgeBaseProperties kbProperties;
    private final KnowledgeDocumentRepository docRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final ChunkMetadataRepository metadataRepository;
    private final Bm25SearchService bm25SearchService;
    private final VectorSearchService vectorSearchService;
    private final EmbeddingStoreService embeddingStoreService;
    private final KnowledgePersistenceMapper mapper;
    private final ObjectMapper jsonMapper;

    @Transactional
    public void clearAllData() {
        log.info("Clearing all knowledge data for full rebuild...");
        metadataRepository.deleteAllInBatch();
        chunkRepository.deleteAllInBatch();
        docRepository.deleteAllInBatch();
    }

    @Transactional
    public Map<String, Integer> syncAll() {
        clearAllData();
        
        int docs = syncDocuments();
        int chunks = syncChunksAndMetadata();
        
        // 1. 重建 Lucene 索引
        try {
            bm25SearchService.rebuildIndex();
        } catch (Exception e) {
            log.error("Lucene rebuild failed: {}", e.getMessage());
        }

        // 2. 重建向量库 (触发 LLM Embedding 调用)
        try {
            embeddingStoreService.rebuildStore();
            vectorSearchService.loadCache(); // 刷新检索层缓存
        } catch (Exception e) {
            log.error("Embedding store rebuild failed: {}", e.getMessage());
        }
        
        return Map.of(
                "syncedDocuments", docs,
                "syncedChunks", chunks,
                "syncedMetadata", chunks
        );
    }

    // --- 内部同步逻辑保持不变 (syncDocuments, syncChunksAndMetadata) ---

    @Transactional
    public int syncDocuments() {
        File catalogFile = new File(kbProperties.getCuratedPath(), "../manifest/doc-catalog.yaml");
        Map<String, KnowledgeDocumentEntity> docMap = new HashMap<>();
        if (catalogFile.exists()) {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            try {
                List<Map<String, Object>> documents = yamlMapper.readValue(catalogFile, new TypeReference<>() {});
                if (documents != null) {
                    for (Map<String, Object> item : documents) {
                        KnowledgeDocumentEntity entity = mapper.toDocumentEntity(item);
                        if (entity != null && entity.getDocId() != null) docMap.put(entity.getDocId(), entity);
                    }
                }
            } catch (Exception e) { log.warn("Catalog parse failed: {}", e.getMessage()); }
        }
        File chunkFile = new File(kbProperties.getChunkPath(), "chunks.jsonl");
        if (chunkFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(chunkFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    SearchableChunk chunk = jsonMapper.readValue(line, SearchableChunk.class);
                    if (chunk.getDocId() == null) continue;
                    if (!docMap.containsKey(chunk.getDocId())) {
                        docMap.put(chunk.getDocId(), KnowledgeDocumentEntity.builder()
                                .docId(chunk.getDocId()).title(chunk.getTitle()).category(chunk.getCategory())
                                .sourceFiles(listToString(chunk.getSourceFiles())).usageScope(listToString(chunk.getUsageScope()))
                                .priority(chunk.getPriority() != null ? chunk.getPriority() : 1).build());
                    }
                }
            } catch (Exception e) { log.warn("Chunk aggregation failed: {}", e.getMessage()); }
        }
        int count = 0;
        for (KnowledgeDocumentEntity entity : docMap.values()) { docRepository.save(entity); count++; }
        return count;
    }

    @Transactional
    public int syncChunksAndMetadata() {
        File chunkFile = new File(kbProperties.getChunkPath(), "chunks.jsonl");
        if (!chunkFile.exists()) return 0;
        int chunkCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(chunkFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                SearchableChunk chunk = jsonMapper.readValue(line, SearchableChunk.class);
                KnowledgeChunkEntity chunkEntity = mapper.toChunkEntity(chunk);
                if (chunkEntity != null) { chunkRepository.save(chunkEntity); chunkCount++; }
                ChunkMetadataEntity metadataEntity = mapper.toMetadataEntity(chunk);
                if (metadataEntity != null) metadataRepository.save(metadataEntity);
            }
        } catch (Exception e) { log.error("Chunk sync failed: {}", e.getMessage()); }
        return chunkCount;
    }

    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(",", list);
    }
}
