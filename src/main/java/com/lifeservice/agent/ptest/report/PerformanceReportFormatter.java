package com.lifeservice.agent.ptest.report;

import com.lifeservice.agent.dto.ptest.ResultAnalyzeResponse;
import com.lifeservice.agent.ptest.analyzer.AnalyzeSceneType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PerformanceReportFormatter {

    public ResultAnalyzeResponse.PerformanceReport format(ResultAnalyzeResponse raw) {
        if (raw == null) return null;

        return ResultAnalyzeResponse.PerformanceReport.builder()
                .overview(generateOverview(raw))
                .keyMetricsSummary(generateKeyMetricsSummary(raw.getKeyMetrics()))
                .assessmentSummary(generateAssessmentSummary(raw.getAssessment()))
                .anomalySummary(raw.getFindings())
                .bottleneckSummary(raw.getSuspectedBottlenecks())
                .actionItems(generateActionItems(raw))
                .build();
    }

    private String generateOverview(ResultAnalyzeResponse raw) {
        String scene = getSceneName(raw.getSceneType());
        return String.format("针对%s场景的性能分析报告已生成。本次压测%s，共覆盖核心指标 %d 项。",
                scene,
                "PASS".equals(raw.getAssessment()) ? "表现稳定" : "存在潜在风险",
                raw.getKeyMetrics() != null ? raw.getKeyMetrics().size() : 0);
    }

    private List<String> generateKeyMetricsSummary(Map<String, Object> metrics) {
        List<String> summary = new ArrayList<>();
        if (metrics == null) return summary;

        Number avgRt = asNumber(metrics.get("AvgRT"));
        if (avgRt != null) {
            summary.add(String.format("平均响应时间: %.2f ms", avgRt.doubleValue()));
        }

        Number p95 = asNumber(metrics.get("P95"));
        if (p95 != null) {
            summary.add(String.format("P95 响应时间: %d ms", p95.longValue()));
        }

        Number tps = asNumber(metrics.get("TPS"));
        if (tps != null) {
            summary.add(String.format("系统吞吐量 (TPS): %.2f/s", tps.doubleValue()));
        }

        Number errorRate = asNumber(metrics.get("ErrorRate"));
        if (errorRate != null) {
            summary.add(String.format("请求错误率: %.2f%%", errorRate.doubleValue() * 100));
        }
        
        return summary;
    }

    private Number asNumber(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return (Number) value;
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String generateAssessmentSummary(String assessment) {
        return switch (assessment) {
            case "PASS" -> "【通过】系统性能满足当前负载要求，未检测到显著瓶颈。";
            case "WARN" -> "【警告】系统在高负载下存在波动或长尾延迟，建议关注优化。";
            case "FAIL" -> "【不通过】核心指标（错误率或 RT）触发阈值，需立即排查修复。";
            default -> "【未知】压测数据不足，无法给出明确结论。";
        };
    }

    private List<String> generateActionItems(ResultAnalyzeResponse raw) {
        List<String> items = new ArrayList<>();
        List<String> suggestions = raw.getSuggestions();
        if (suggestions == null || suggestions.isEmpty()) {
            items.add("暂无紧急改进项，建议持续监控。");
            return items;
        }

        AnalyzeSceneType sceneType = raw.getSceneType();
        
        // 场景特定增强
        if (sceneType == AnalyzeSceneType.SECKILL) {
            items.add("核心建议：排查 Redis Lua 脚本原子性及 RabbitMQ 消费堆积情况。");
        } else if (sceneType == AnalyzeSceneType.SHOP_READ) {
            items.add("核心建议：优化 CacheClient 缓存预热及回源查询索引。");
        } else if (sceneType == AnalyzeSceneType.LOGIN) {
            items.add("核心建议：评估 Redis 写入吞吐量及 Token 存储空间。");
        }

        // 限制输出条数
        items.addAll(suggestions.stream().limit(3).collect(Collectors.toList()));
        
        return items.stream().distinct().collect(Collectors.toList());
    }

    private String getSceneName(AnalyzeSceneType type) {
        if (type == null) return "通用业务";
        return switch (type) {
            case SECKILL -> "秒杀抢购";
            case SHOP_READ -> "商铺查询";
            case LOGIN -> "登录鉴权";
            default -> "通用业务";
        };
    }
}
