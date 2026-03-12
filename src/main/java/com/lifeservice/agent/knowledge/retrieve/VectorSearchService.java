package com.lifeservice.agent.knowledge.retrieve;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeservice.agent.config.KnowledgeBaseProperties;
import com.lifeservice.agent.infra.repository.KnowledgeChunkRepository;
import com.lifeservice.agent.knowledge.model.SearchCandidate;
import com.lifeservice.agent.knowledge.model.SearchOptions;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import com.lifeservice.agent.knowledge.model.VectorChunk;
import com.lifeservice.agent.knowledge.store.KnowledgePersistenceMapper;
import com.lifeservice.agent.llm.embedding.EmbeddingClientAdapter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final KnowledgeBaseProperties kbProperties;
    private final EmbeddingClientAdapter embeddingClient;
    private final KnowledgeChunkRepository chunkRepository;
    private final KnowledgePersistenceMapper mapper;
    private final ObjectMapper objectMapper;

    private List<VectorChunk> vectorCache = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadCache();
    }

    public void loadCache() {
        File file = new File(Paths.get(kbProperties.getIndexPath(), "vector", "embeddings.jsonl").toString());
        if (!file.exists()) {
            log.warn("Vector index file not found at {}", file.getAbsolutePath());
            return;
        }

        List<VectorChunk> newCache = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                newCache.add(objectMapper.readValue(line, VectorChunk.class));
            }
            this.vectorCache = newCache;
            log.info("Loaded {} vectors into memory cache.", vectorCache.size());
        } catch (Exception e) {
            log.error("Failed to load vector cache: {}", e.getMessage());
        }
    }

    public List<SearchCandidate> search(SearchOptions options) {
        if (vectorCache.isEmpty()) return new ArrayList<>();

        List<Double> queryVector = embeddingClient.embed(options.getQuery());
        if (queryVector == null) return new ArrayList<>();

        // 1. Calculate similarities
        List<ScoredId> scoredIds = vectorCache.stream()
                .map(vc -> new ScoredId(vc.getChunkId(), cosineSimilarity(queryVector, vc.getEmbedding())))
                .sorted(Comparator.comparing(ScoredId::getScore).reversed())
                .limit(options.getTopK() * 2)
                .collect(Collectors.toList());

        // 2. Hydrate chunks from DB
        List<SearchCandidate> results = new ArrayList<>();
        for (ScoredId si : scoredIds) {
            chunkRepository.findById(si.getId()).ifPresent(entity -> {
                SearchableChunk chunk = SearchableChunk.builder()
                        .chunkId(entity.getChunkId())
                        .docId(entity.getDocId())
                        .title(entity.getTitle())
                        .content(entity.getContent())
                        .section(entity.getSection())
                        .category(entity.getCategory())
                        .knowledgeType(entity.getKnowledgeType())
                        .build();
                
                results.add(SearchCandidate.builder()
                        .chunk(chunk)
                        .score(si.getScore())
                        .engine("vector")
                        .scoreBreakdown(Map.of("cosine_similarity", si.getScore()))
                        .build());
            });
        }

        return results;
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            normA += Math.pow(v1.get(i), 2);
            normB += Math.pow(v2.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @lombok.Value
    private static class ScoredId {
        String id;
        double score;
    }
}
