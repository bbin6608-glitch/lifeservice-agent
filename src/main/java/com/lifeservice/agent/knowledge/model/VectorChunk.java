package com.lifeservice.agent.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 带有向量信息的知识碎片
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorChunk {
    private String chunkId;
    private List<Double> embedding;
}
