package com.lifeservice.agent.knowledge.retrieve;

import com.lifeservice.agent.knowledge.model.SearchCandidate;
import com.lifeservice.agent.knowledge.model.SearchOptions;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 结构化检索服务 - 基于属性过滤和 DB 索引
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StructuredSearchService {

    private final ChunkFileLoader chunkFileLoader;

    public List<SearchCandidate> search(SearchOptions options) {
        Map<String, Object> filters = options.getFilters();
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>();
        }

        List<SearchableChunk> allChunks;
        try {
            allChunks = chunkFileLoader.loadAll();
        } catch (Exception e) {
            log.error("Failed to load chunks for structured search: {}", e.getMessage());
            return new ArrayList<>();
        }

        // 目前基于内存过滤，后期改为 DB SQL 过滤
        return allChunks.stream()
                .filter(chunk -> matchFilters(chunk, filters))
                .map(chunk -> SearchCandidate.builder()
                        .chunk(chunk)
                        .score(1.0) // 结构化匹配一般为 0/1 分，权重在 Fusion 中体现
                        .engine("structured")
                        .build())
                .collect(Collectors.toList());
    }

    private boolean matchFilters(SearchableChunk chunk, Map<String, Object> filters) {
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if ("category".equals(key) && !value.equals(chunk.getCategory())) return false;
            if ("docId".equals(key) && !value.equals(chunk.getDocId())) return false;
            // 更多过滤条件...
        }
        return true;
    }
}
