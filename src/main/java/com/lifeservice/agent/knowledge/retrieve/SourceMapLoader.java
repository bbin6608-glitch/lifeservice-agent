package com.lifeservice.agent.knowledge.retrieve;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeservice.agent.config.KnowledgeBaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceMapLoader {

    private final KnowledgeBaseProperties kbProperties;
    private final ObjectMapper objectMapper;
    private final Map<String, List<String>> sourceMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        load();
    }

    public synchronized void load() {
        String manifestPath = kbProperties.getIndexPath().replace("index", "manifest");
        File manifestDir = new File(manifestPath);
        File sourceMapFile = new File(manifestDir, "source-map.json");

        if (!sourceMapFile.exists()) {
            log.info("Source map file not found at: {}, skipping source file enrichment.", sourceMapFile.getAbsolutePath());
            return;
        }

        try {
            Map<String, List<String>> data = objectMapper.readValue(sourceMapFile, new TypeReference<Map<String, List<String>>>() {});
            sourceMap.clear();
            sourceMap.putAll(data);
            log.info("Successfully loaded {} source map entries.", sourceMap.size());
        } catch (Exception e) {
            log.error("Failed to load source map: {}", e.getMessage());
        }
    }

    public List<String> getSourceFiles(String docId, String title) {
        if (sourceMap.containsKey(docId)) {
            return sourceMap.get(docId);
        }
        if (title != null && sourceMap.containsKey(title)) {
            return sourceMap.get(title);
        }
        return new ArrayList<>();
    }
}
