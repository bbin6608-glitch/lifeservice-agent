package com.lifeservice.agent.knowledge.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeservice.agent.config.KnowledgeBaseProperties;
import com.lifeservice.agent.knowledge.build.KnowledgeIndexService;
import com.lifeservice.agent.knowledge.model.KnowledgeChunk;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class LuceneKnowledgeIndexService implements KnowledgeIndexService {

    private final KnowledgeBaseProperties kbProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void rebuild(List<KnowledgeChunk> chunks) {
        log.info("Persisting {} chunks to chunks.jsonl...", chunks.size());
        
        File chunkDir = new File(kbProperties.getChunkPath());
        if (!chunkDir.exists()) chunkDir.mkdirs();
        
        File chunkFile = new File(chunkDir, "chunks.jsonl");
        
        try (PrintWriter writer = new PrintWriter(chunkFile)) {
            for (KnowledgeChunk chunk : chunks) {
                SearchableChunk searchable = SearchableChunk.builder()
                        .chunkId(chunk.getChunkId())
                        .docId(chunk.getDocId())
                        .title(chunk.getTitle())
                        .section(chunk.getSection())
                        .content(chunk.getContent())
                        .category(chunk.getCategory())
                        .knowledgeType(chunk.getKnowledgeType())
                        .sourceMode(chunk.getSourceMode())
                        .confidenceLevel(chunk.getConfidenceLevel())
                        .usageScope(chunk.getMetadata() != null ? chunk.getMetadata().getUsageScope() : null)
                        .sourceFiles(chunk.getMetadata() != null ? chunk.getMetadata().getSourceFiles() : null)
                        .priority(chunk.getMetadata() != null ? chunk.getMetadata().getPriority() : 1)
                        .build();
                
                writer.println(objectMapper.writeValueAsString(searchable));
            }
            log.info("Successfully wrote {} chunks to {}", chunks.size(), chunkFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to write chunks.jsonl: {}", e.getMessage(), e);
        }
    }
}
