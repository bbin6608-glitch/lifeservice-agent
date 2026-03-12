package com.lifeservice.agent.dto.ptest;

import com.lifeservice.agent.ptest.analyzer.AnalyzeSceneType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultAnalyzeResponse {
    private String summary;
    private List<String> findings;
    private List<String> suggestions;
    private AnalyzeSceneType sceneType;
    private Map<String, Object> keyMetrics;
    private List<String> suspectedBottlenecks;
    private String assessment; // PASS, WARN, FAIL
    
    // 新增报告字段
    private PerformanceReport report;
    
    // 新增 Markdown 报告字段
    private String markdownReport;
    
    // 新增报告文件相关字段
    private String reportFileName;
    private String reportFilePath;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceReport {
        private String overview;
        private List<String> keyMetricsSummary;
        private String assessmentSummary;
        private List<String> anomalySummary;
        private List<String> bottleneckSummary;
        private List<String> actionItems;
    }
}
