package com.lifeservice.agent.ptest.report;

import com.lifeservice.agent.dto.ptest.ResultAnalyzeResponse;
import com.lifeservice.agent.ptest.analyzer.AnalyzeSceneType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MarkdownPerformanceReportFormatter {

    public String format(ResultAnalyzeResponse response, String endpoint) {
        if (response == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("# 压测分析报告\n\n");

        // 1. 压测概览
        sb.append("## 1. 压测概览\n");
        sb.append("- **接口地址**: `").append(endpoint != null ? endpoint : "未知").append("`\n");
        sb.append("- **场景类型**: ").append(getSceneDisplayName(response.getSceneType())).append("\n");
        sb.append("- **评估结果**: ").append(getAssessmentDisplayName(response.getAssessment())).append("\n");
        sb.append("- **核心摘要**: ").append(response.getSummary() != null ? response.getSummary() : "暂无摘要").append("\n\n");

        // 2. 关键指标
        sb.append("## 2. 关键指标\n");
        Map<String, Object> metrics = response.getKeyMetrics();
        if (metrics != null && !metrics.isEmpty()) {
            appendMetric(sb, "平均响应时间 (AvgRT)", metrics.get("AvgRT"), " ms");
            appendMetric(sb, "P95 响应时间", metrics.get("P95"), " ms");
            appendMetric(sb, "系统吞吐量 (TPS)", metrics.get("TPS"), "/s");
            appendPercentageMetric(sb, "请求错误率 (ErrorRate)", metrics.get("ErrorRate"));
        } else {
            sb.append("- 当前数据不足\n");
        }
        sb.append("\n");

        // 3. 结果评估
        sb.append("## 3. 结果评估\n");
        if (response.getReport() != null) {
            sb.append("- **结论**: ").append(response.getReport().getAssessmentSummary()).append("\n");
            sb.append("- **详情**: ").append(response.getReport().getOverview()).append("\n");
        } else {
            sb.append("- 当前数据不足\n");
        }
        sb.append("\n");

        // 4. 异常摘要
        sb.append("## 4. 异常摘要\n");
        appendList(sb, response.getFindings(), "暂无明显异常");
        sb.append("\n");

        // 5. 疑似瓶颈
        sb.append("## 5. 疑似瓶颈\n");
        appendList(sb, response.getSuspectedBottlenecks(), "暂无明显瓶颈");
        sb.append("\n");

        // 6. 行动建议
        sb.append("## 6. 行动建议\n");
        if (response.getReport() != null && response.getReport().getActionItems() != null) {
            appendList(sb, response.getReport().getActionItems(), "暂无更多建议");
        } else {
            appendList(sb, response.getSuggestions(), "暂无更多建议");
        }
        sb.append("\n");

        sb.append("---\n");
        sb.append("> 本报告由 Lifeservice Agent 自动生成\n");

        return sb.toString();
    }

    private void appendMetric(StringBuilder sb, String label, Object value, String unit) {
        Number num = asNumber(value);
        if (num != null) {
            sb.append("- **").append(label).append("**: ").append(String.format("%.2f", num.doubleValue())).append(unit).append("\n");
        }
    }

    private void appendPercentageMetric(StringBuilder sb, String label, Object value) {
        Number num = asNumber(value);
        if (num != null) {
            sb.append("- **").append(label).append("**: ").append(String.format("%.2f%%", num.doubleValue() * 100)).append("\n");
        }
    }

    private void appendList(StringBuilder sb, List<String> list, String emptyMsg) {
        if (list == null || list.isEmpty()) {
            sb.append("- ").append(emptyMsg).append("\n");
        } else {
            for (String item : list) {
                sb.append("- ").append(item).append("\n");
            }
        }
    }

    private String getSceneDisplayName(AnalyzeSceneType type) {
        if (type == null) return "未知场景";
        return switch (type) {
            case SECKILL -> "秒杀抢购";
            case SHOP_READ -> "商铺查询";
            case LOGIN -> "登录鉴权";
            default -> "通用业务";
        };
    }

    private String getAssessmentDisplayName(String assessment) {
        if (assessment == null) return "未知";
        return switch (assessment) {
            case "PASS" -> "✅ 通过";
            case "WARN" -> "⚠️ 警告";
            case "FAIL" -> "❌ 不通过";
            default -> assessment;
        };
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
}
