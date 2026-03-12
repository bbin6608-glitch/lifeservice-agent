package com.lifeservice.agent.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 搜索配置选项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchOptions {
    /**
     * 原始查询
     */
    private String query;
    
    /**
     * 返回最大数量
     */
    @Builder.Default
    private int topK = 5;
    
    /**
     * 最低分数阈值
     */
    @Builder.Default
    private double minScore = 0.0;
    
    /**
     * 业务上下文过滤 (如 category, usageScope)
     */
    private Map<String, Object> filters;
    
    /**
     * 强制启用的引擎 (bm25, vector, structured)
     */
    private List<String> enabledEngines;
    
    /**
     * 针对不同引擎的权重分配
     */
    private Map<String, Double> engineWeights;

    public static SearchOptions defaultFor(String query, int topK) {
        return SearchOptions.builder()
                .query(query)
                .topK(topK)
                .build();
    }
}
