package com.lifeservice.agent.controller;

import com.lifeservice.agent.knowledge.build.*;
import com.lifeservice.agent.knowledge.model.KnowledgeChunk;
import com.lifeservice.agent.knowledge.model.KnowledgeDocument;
import com.lifeservice.agent.knowledge.model.KnowledgeFact;
import com.lifeservice.agent.knowledge.model.RawSourceFile;
import com.lifeservice.agent.knowledge.retrieve.ChunkFileLoader;
import com.lifeservice.agent.knowledge.store.KnowledgeSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "知识库管理")
@RestController
@RequiredArgsConstructor
public class KnowledgeAdminController {

    private final SnapshotSourceLoader sourceLoader;
    private final RawSourceParser sourceParser;
    private final KnowledgeDocGenerator docGenerator;
    private final ChunkGenerator chunkGenerator;
    private final KnowledgeIndexService indexService;
    private final ChunkFileLoader chunkFileLoader;
    private final KnowledgeSyncService syncService;

    @Operation(summary = "重建知识库")
    @PostMapping("/api/kb/rebuild")
    public Map<String, Object> rebuild() {
        // 1. Load
        List<RawSourceFile> rawFiles = sourceLoader.loadAll();

        // 2. Parse
        List<KnowledgeFact> facts = new ArrayList<>();
        for (RawSourceFile file : rawFiles) {
            if (sourceParser.supports(file.getFileName())) {
                facts.addAll(sourceParser.parse(file));
            }
        }

        // 3. Generate Docs
        List<KnowledgeDocument> docs = docGenerator.generate(facts);

        // 4. Chunk
        List<KnowledgeChunk> chunks = chunkGenerator.chunk(docs);

        // 5. Index
        indexService.rebuild(chunks);

        // 6. Reload cache
        boolean chunkCacheReloaded = false;
        try {
            chunkFileLoader.reload();
            chunkCacheReloaded = true;
        } catch (Exception e) {
            log.error("Failed to reload chunk cache after rebuild: {}", e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("chunkCacheReloaded", chunkCacheReloaded);
        result.put("stats", Map.of(
                "rawFiles", rawFiles.size(),
                "facts", facts.size(),
                "docs", docs.size(),
                "chunks", chunks.size()
        ));
        
        return result;
    }

    @Operation(summary = "同步知识库到数据库")
    @PostMapping("/api/kb/sync-db")
    public Map<String, Object> syncDb() {
        Map<String, Integer> stats = syncService.syncAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("stats", stats);
        
        return result;
    }
}
