package com.lifeservice.agent.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 统一搜索结果候选项 - 增强版（包含详细分值追踪）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCandidate {
    private SearchableChunk chunk;
    
    /**
     * 最终融合后的总分
     */
    private double score;
    
    /**
     * 各引擎/阶段的原始分值与归一化分值
     */
    private Double bm25Score;
    private Double vectorScore;
    private Double rerankScore;
    
    /**
     * 来源引擎追踪
     */
    private String engine;
    
    /**
     * 详细分值构成图 (用于日志/调试)
     */
    private Map<String, Double> scoreBreakdown;
}
