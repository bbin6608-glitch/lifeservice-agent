package com.lifeservice.agent.ptest.analyzer.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ParsedPerformanceReport {
    private long totalSamples;
    private long successCount;
    private long errorCount;
    private double avgElapsed;
    private long maxElapsed;
    private long minElapsed;
    private double p95Elapsed;
    private double p99Elapsed;
    private double throughputPerSecond;
    private double errorRate;
    private Map<String, Integer> responseCodeCounts;
}
