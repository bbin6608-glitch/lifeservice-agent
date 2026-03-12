package com.lifeservice.agent.knowledge.retrieve;

import com.lifeservice.agent.knowledge.model.SearchCandidate;
import com.lifeservice.agent.knowledge.model.SearchOptions;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合检索服务 - 第五轮增强：透明化打分链路与权重初调
 */
@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class HybridKnowledgeSearchService implements KnowledgeSearchService {

    private final Bm25SearchService bm25SearchService;
    private final VectorSearchService vectorSearchService;

    // --- 当前融合权重设定 ---
    private static final double W_BM25 = 0.45;
    private static final double W_VECTOR = 0.35;
    private static final double W_RERANK = 0.20;

    @Override
    public List<SearchCandidate> search(SearchOptions options) {
        log.info("--- Hybrid Search Detail Trace: [{}] ---", options.getQuery());

        // 1. 双路并行召回
        List<SearchCandidate> bm25List = bm25SearchService.search(options);
        List<SearchCandidate> vectorList = vectorSearchService.search(options);

        // 2. 归一化处理 (使用各路最高分作为分母)
        double maxBm25 = bm25List.stream().mapToDouble(SearchCandidate::getScore).max().orElse(1.0);
        double maxVector = vectorList.stream().mapToDouble(SearchCandidate::getScore).max().orElse(1.0);

        Map<String, SearchCandidate> masterMap = new HashMap<>();

        // 3. 处理 BM25 结果 (45% 权重)
        for (SearchCandidate c : bm25List) {
            double norm = c.getScore() / (maxBm25 > 0 ? maxBm25 : 1.0);
            c.setBm25Score(norm);
            c.setVectorScore(0.0);
            c.setScore(norm * W_BM25);
            masterMap.put(c.getChunk().getChunkId(), c);
        }

        // 4. 处理 Vector 结果 (35% 权重) 并合并
        for (SearchCandidate v : vectorList) {
            double norm = v.getScore() / (maxVector > 0 ? maxVector : 1.0);
            String cid = v.getChunk().getChunkId();
            if (masterMap.containsKey(cid)) {
                SearchCandidate existing = masterMap.get(cid);
                existing.setVectorScore(norm);
                existing.setScore(existing.getScore() + (norm * W_VECTOR));
                existing.setEngine("hybrid");
            } else {
                v.setVectorScore(norm);
                v.setBm25Score(0.0);
                v.setScore(norm * W_VECTOR);
                v.setEngine("vector-only");
                masterMap.put(cid, v);
            }
        }

        // 5. 业务重排序 (20% 权重调节空间)
        List<SearchCandidate> finalCandidates = masterMap.values().stream()
                .map(c -> applyRerank(c, options.getQuery()))
                .sorted(Comparator.comparing(SearchCandidate::getScore).reversed())
                .limit(options.getTopK())
                .collect(Collectors.toList());

        // 6. 打印打分详情日志
        finalCandidates.forEach(c -> log.debug("[SearchHit] ID: {}, Score: {:.4f} (BM25: {:.2f}*0.45, Vector: {:.2f}*0.35, Rerank: {:.2f}*0.20), Section: {}",
                c.getChunk().getChunkId(), c.getScore(), c.getBm25Score(), c.getVectorScore(), c.getRerankScore(), c.getChunk().getSection()));

        return finalCandidates;
    }

    private SearchCandidate applyRerank(SearchCandidate candidate, String query) {
        SearchableChunk chunk = candidate.getChunk();
        double rScore = 0.5; // 默认基准

        // 奖励规则
        if ("IMPLEMENTATION".equalsIgnoreCase(chunk.getKnowledgeType())) rScore += 0.3;
        if (chunk.isHighValueSection()) rScore += 0.2;
        
        // 惩罚规则
        if (chunk.isLowValueSection()) rScore -= 0.4;

        // 特定关键词匹配
        if (query.contains("app1") || query.contains("app2")) {
            if (chunk.getTitle().contains("differences") || chunk.getSection().contains("差异")) rScore += 0.3;
        }

        candidate.setRerankScore(Math.max(0, rScore));
        // 最终公式融入 Rerank：Score_old + (RScore * W_RERANK)
        candidate.setScore(candidate.getScore() + (candidate.getRerankScore() * W_RERANK));
        return candidate;
    }
}
