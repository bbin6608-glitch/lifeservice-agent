package com.lifeservice.agent.knowledge.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeservice.agent.config.KnowledgeBaseProperties;
import com.lifeservice.agent.infra.persistence.entity.ChunkEmbeddingEntity;
import com.lifeservice.agent.infra.persistence.entity.KnowledgeChunkEntity;
import com.lifeservice.agent.infra.repository.ChunkEmbeddingRepository;
import com.lifeservice.agent.infra.repository.KnowledgeChunkRepository;
import com.lifeservice.agent.knowledge.model.VectorChunk;
import com.lifeservice.agent.llm.embedding.EmbeddingClientAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingStoreService {

    private final KnowledgeBaseProperties kbProperties;
    private final KnowledgeChunkRepository chunkRepository;
    private final ChunkEmbeddingRepository embeddingRepository;
    private final EmbeddingClientAdapter embeddingClient;
    private final ObjectMapper objectMapper;

    // 白名单分类
    private static final Set<String> WHITE_LIST_CATEGORIES = Set.of(
            "core-flow", "middleware", "source-guide", "method-card", "runtime", "implementation", "overview"
    );

    @Transactional
    public void rebuildStore() {
        log.info("Starting embedding store full rebuild (Relaxed Rules)...");
        
        embeddingRepository.deleteAllInBatch();
        
        List<KnowledgeChunkEntity> allChunks = chunkRepository.findAll();
        List<KnowledgeChunkEntity> filtered = allChunks.stream()
                .filter(c -> {
                    // 1. 人工知识必选
                    if ("IMPLEMENTATION".equals(c.getKnowledgeType())) return true;
                    
                    // 2. 白名单分类必选
                    if (WHITE_LIST_CATEGORIES.contains(c.getCategory())) return true;
                    
                    // 3. 兜底逻辑：即使是 raw-import，但如果是核心源码或精修文档也入选
                    // 排除 api_mappings, tree.txt 等纯索引类噪音
                    String title = c.getTitle() != null ? c.getTitle().toLowerCase() : "";
                    if (title.endsWith(".java") || title.endsWith(".lua") || title.endsWith(".md")) {
                        if (!title.contains("api_mappings") && !title.contains("tree.txt") && !title.contains("java_files")) {
                            return true;
                        }
                    }
                    
                    return false;
                })
                .collect(Collectors.toList());

        log.info("Filtered {} chunks for vectorization after relaxation", filtered.size());

        if (filtered.isEmpty()) return;

        int batchSize = 10;
        List<VectorChunk> vectorChunks = new ArrayList<>();

        for (int i = 0; i < filtered.size(); i += batchSize) {
            int end = Math.min(i + batchSize, filtered.size());
            List<KnowledgeChunkEntity> batch = filtered.subList(i, end);
            List<String> contents = batch.stream()
                    .map(c -> "[" + c.getCategory() + "] " + c.getTitle() + " - " + c.getSection() + "\n" + c.getContent())
                    .collect(Collectors.toList());

            List<List<Double>> embeddings = embeddingClient.embedBatch(contents);
            if (embeddings != null && embeddings.size() == batch.size()) {
                for (int j = 0; j < batch.size(); j++) {
                    KnowledgeChunkEntity chunk = batch.get(j);
                    List<Double> vector = embeddings.get(j);
                    
                    embeddingRepository.save(ChunkEmbeddingEntity.builder()
                            .chunkId(chunk.getChunkId())
                            .embedding(vector.toString())
                            .model("text-embedding-v3")
                            .build());
                    
                    vectorChunks.add(VectorChunk.builder()
                            .chunkId(chunk.getChunkId())
                            .embedding(vector)
                            .build());
                }
            }
            log.info("Progress: {}/{}", end, filtered.size());
        }

        saveToLocalFile(vectorChunks);
        log.info("Embedding store rebuild completed. Total vectors: {}", vectorChunks.size());
    }

    private void saveToLocalFile(List<VectorChunk> vectorChunks) {
        try {
            File dir = new File(Paths.get(kbProperties.getIndexPath(), "vector").toString());
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "embeddings.jsonl");
            try (PrintWriter writer = new PrintWriter(file)) {
                for (VectorChunk vc : vectorChunks) {
                    writer.println(objectMapper.writeValueAsString(vc));
                }
            }
        } catch (Exception e) {
            log.error("Failed to save vectors: {}", e.getMessage());
        }
    }
}
