package com.lifeservice.agent.knowledge.retrieve;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeservice.agent.config.KnowledgeBaseProperties;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChunkFileLoader {

    private final KnowledgeBaseProperties kbProperties;
    private final ObjectMapper objectMapper;
    private final List<SearchableChunk> cache = new CopyOnWriteArrayList<>();

    public synchronized List<SearchableChunk> loadAll() {
        if (!cache.isEmpty()) {
            return cache;
        }
        reload();
        return cache;
    }

    public synchronized void reload() {
        String chunkPath = kbProperties.getChunkPath();
        File chunkDir = new File(chunkPath);
        File chunkFile = new File(chunkDir, "chunks.jsonl");

        if (!chunkFile.exists()) {
            log.error("Chunk file not found at: {}", chunkFile.getAbsolutePath());
            throw new RuntimeException("知识库尚未构建完成或 chunk 文件不存在 at: " + chunkFile.getAbsolutePath());
        }

        List<SearchableChunk> newChunks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(chunkFile, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                try {
                    SearchableChunk chunk = objectMapper.readValue(line, SearchableChunk.class);
                    newChunks.add(chunk);
                } catch (Exception e) {
                    log.warn("Failed to parse chunk at line {}: {}", lineNumber, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error reading chunk file: {}", e.getMessage(), e);
            throw new RuntimeException("读取知识库文件失败: " + e.getMessage());
        }

        if (newChunks.isEmpty()) {
            log.warn("Loaded 0 chunks from {}", chunkFile.getAbsolutePath());
        } else {
            log.info("Successfully loaded {} chunks from {}", newChunks.size(), chunkFile.getAbsolutePath());
        }

        cache.clear();
        cache.addAll(newChunks);
    }
}
