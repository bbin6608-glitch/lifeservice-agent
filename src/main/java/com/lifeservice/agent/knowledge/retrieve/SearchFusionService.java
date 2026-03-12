package com.lifeservice.agent.knowledge.retrieve;

import com.lifeservice.agent.knowledge.model.SearchCandidate;
import com.lifeservice.agent.knowledge.model.SearchOptions;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 检索结果融合服务 - 实现 RRF (Reciprocal Rank Fusion)
 */
@Slf4j
@Service
public class SearchFusionService {

    private static final int RRF_K = 60; // RRF 常用常数

    public List<SearchCandidate> fuse(List<List<SearchCandidate>> resultSets, SearchOptions options) {
        if (resultSets == null || resultSets.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, SearchableChunk> chunkCache = new HashMap<>();

        for (List<SearchCandidate> resultSet : resultSets) {
            for (int rank = 0; rank < resultSet.size(); rank++) {
                SearchCandidate candidate = resultSet.get(rank);
                String chunkId = candidate.getChunk().getChunkId();
                chunkCache.putIfAbsent(chunkId, candidate.getChunk());

                // RRF score calculation: 1 / (K + rank)
                double currentScore = 1.0 / (RRF_K + rank + 1);
                rrfScores.merge(chunkId, currentScore, Double::sum);
            }
        }

        return rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(options.getTopK())
                .map(entry -> SearchCandidate.builder()
                        .chunk(chunkCache.get(entry.getKey()))
                        .score(entry.getValue())
                        .engine("hybrid-fusion")
                        .build())
                .collect(Collectors.toList());
    }
}
