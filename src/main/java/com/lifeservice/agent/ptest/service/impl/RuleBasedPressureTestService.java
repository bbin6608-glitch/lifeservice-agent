package com.lifeservice.agent.ptest.service.impl;

import com.lifeservice.agent.dto.ptest.*;
import com.lifeservice.agent.knowledge.model.SearchCandidate;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import com.lifeservice.agent.knowledge.retrieve.KnowledgeSearchService;
import com.lifeservice.agent.ptest.analyzer.AnalyzeSceneClassifier;
import com.lifeservice.agent.ptest.analyzer.AnalyzeSceneType;
import com.lifeservice.agent.ptest.analyzer.JtlCsvReportParser;
import com.lifeservice.agent.ptest.analyzer.model.ParsedPerformanceReport;
import com.lifeservice.agent.ptest.jmeter.JmeterTemplateSelector;
import com.lifeservice.agent.ptest.jmeter.JmeterTemplateService;
import com.lifeservice.agent.ptest.jmeter.JmeterTemplateType;
import com.lifeservice.agent.ptest.jmeter.JmxFileWriter;
import com.lifeservice.agent.ptest.planner.EndpointSceneClassifier;
import com.lifeservice.agent.ptest.planner.PressureSceneType;
import com.lifeservice.agent.ptest.report.MarkdownPerformanceReportFormatter;
import com.lifeservice.agent.ptest.report.PerformanceReportFileWriter;
import com.lifeservice.agent.ptest.report.PerformanceReportFormatter;
import com.lifeservice.agent.ptest.service.PressureTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleBasedPressureTestService implements PressureTestService {

    private final EndpointSceneClassifier sceneClassifier;
    private final JmeterTemplateSelector templateSelector;
    private final JmeterTemplateService templateService;
    private final JmxFileWriter jmxFileWriter;
    private final AnalyzeSceneClassifier analyzeClassifier;
    private final JtlCsvReportParser reportParser;
    private final PerformanceReportFormatter reportFormatter;
    private final MarkdownPerformanceReportFormatter markdownReportFormatter;
    private final PerformanceReportFileWriter reportFileWriter;
    private final KnowledgeSearchService searchService;

    @Override
    public PressurePlanResponse generatePlan(PressurePlanRequest request) {
        PressureSceneType sceneType = sceneClassifier.classify(request.getEndpoint());
        PressurePlanResponse.PressurePlanResponseBuilder builder = PressurePlanResponse.builder()
                .endpoint(request.getEndpoint())
                .sceneType(sceneType);

        switch (sceneType) {
            case SECKILL: buildSeckillPlan(builder); break;
            case SHOP_READ: buildShopReadPlan(builder); break;
            case LOGIN: buildLoginPlan(builder); break;
            default: buildGeneralPlan(builder); break;
        }

        augmentPlanWithKnowledge(builder, request.getEndpoint());
        return builder.build();
    }

    @Override
    public JmxGenerateResponse generateJmx(JmxGenerateRequest request) {
        PressureSceneType sceneType = request.getScenarioType() != null ? 
                PressureSceneType.valueOf(request.getScenarioType().toUpperCase()) : 
                sceneClassifier.classify(request.getEndpoint());

        PressurePlanRequest planReq = new PressurePlanRequest();
        planReq.setEndpoint(request.getEndpoint());
        PressurePlanResponse plan = generatePlan(planReq);

        JmeterTemplateType templateType = templateSelector.select(request.getEndpoint(), request.getScenarioType());
        String jmxContent = templateService.generate(request.getEndpoint(), templateType, request.getVariables());
        
        // 持久化 JMX 到磁盘
        String fileName = templateType.name().toLowerCase() + "-plan.jmx";
        String filePath = jmxFileWriter.write(fileName, jmxContent);

        return JmxGenerateResponse.builder()
                .fileName(fileName)
                .jmxContent(jmxContent)
                .sceneType(sceneType)
                .summary(plan.getSummary())
                .notes(List.of(
                        "场景类型: " + sceneType,
                        "文件路径: " + (filePath != null ? filePath : "写入失败"),
                        "核心风险: " + (plan.getRisks().isEmpty() ? "暂无" : plan.getRisks().get(0))
                ))
                .build();
    }

    @Override
    public ResultAnalyzeResponse analyze(ResultAnalyzeRequest request) {
        if (request.getReportFilePath() == null || request.getReportFilePath().isEmpty()) {
            return ResultAnalyzeResponse.builder().summary("未提供报告文件路径。").assessment("FAIL").build();
        }

        Optional<ParsedPerformanceReport> reportOpt = reportParser.parse(request.getReportFilePath());
        if (reportOpt.isEmpty()) return ResultAnalyzeResponse.builder().summary("报告解析失败。").assessment("FAIL").build();

        ParsedPerformanceReport report = reportOpt.get();
        AnalyzeSceneType sceneType = analyzeClassifier.classify(request.getEndpoint());
        List<String> findings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        List<String> bottlenecks = new ArrayList<>();

        String assessment = performBasicAssessment(report, findings);

        switch (sceneType) {
            case SECKILL: analyzeSeckillEnhanced(report, findings, suggestions, bottlenecks); break;
            case SHOP_READ: analyzeShopReadEnhanced(report, findings, suggestions, bottlenecks); break;
            default: analyzeGeneral(report, findings, suggestions, bottlenecks); break;
        }

        if ("FAIL".equals(assessment) && suggestions.isEmpty()) {
            generateEmergencySuggestions(sceneType, suggestions);
        }

        String summary = String.format("本次%s接口压测共采样 %d 次，成功率 %.2f%%，平均 RT %.2fms，TPS %.2f。",
                getSceneDisplayName(sceneType), report.getTotalSamples(), (1 - report.getErrorRate()) * 100, report.getAvgElapsed(), report.getThroughputPerSecond());

        ResultAnalyzeResponse response = ResultAnalyzeResponse.builder()
                .summary(summary).findings(findings).suggestions(new ArrayList<>(new LinkedHashSet<>(suggestions)))
                .sceneType(sceneType).suspectedBottlenecks(new ArrayList<>(new LinkedHashSet<>(bottlenecks))).assessment(assessment)
                .keyMetrics(Map.of("TPS", report.getThroughputPerSecond(), "AvgRT", report.getAvgElapsed(), "P95", report.getP95Elapsed(), "ErrorRate", report.getErrorRate()))
                .build();

        response.setReport(reportFormatter.format(response));
        String mdContent = markdownReportFormatter.format(response, request.getEndpoint());
        response.setMarkdownReport(mdContent);
        try {
            PerformanceReportFileWriter.FileWriteResult writeResult = reportFileWriter.write(request.getEndpoint(), sceneType, mdContent);
            if (writeResult != null) {
                response.setReportFileName(writeResult.getFileName());
                response.setReportFilePath(writeResult.getFilePath());
            }
        } catch (Exception e) { log.error("Report save failed: {}", e.getMessage()); }

        return response;
    }

    private void analyzeSeckillEnhanced(ParsedPerformanceReport report, List<String> findings, List<String> suggestions, List<String> bottlenecks) {
        if (report.getErrorRate() > 0.01) {
            findings.add("秒杀请求存在较高错误率 (" + String.format("%.2f%%", report.getErrorRate() * 100) + ")");
            deepAugment("秒杀下单数据库乐观锁冲突、一人一单校验异常、库存超卖风险、MQ 异步处理积压", suggestions, bottlenecks, "ERROR_RATE");
        }
        if (report.getAvgElapsed() > 100) {
            findings.add("秒杀入口 RT 偏高 (" + String.format("%.2fms", report.getAvgElapsed()) + ")");
            deepAugment("Redis Lua 脚本执行耗时、热点 Key 访问压力", suggestions, bottlenecks, "LATENCY");
        }
    }

    private void analyzeShopReadEnhanced(ParsedPerformanceReport report, List<String> findings, List<String> suggestions, List<String> bottlenecks) {
        if (report.getP95Elapsed() > 200) {
            findings.add("商铺读接口尾延迟明显");
            deepAugment("商铺查询缓存击穿风险、CacheClient 逻辑过期重建延迟", suggestions, bottlenecks, "LATENCY");
        }
    }

    private void deepAugment(String query, List<String> suggestions, List<String> bottlenecks, String symptom) {
        try {
            List<SearchCandidate> candidates = searchService.search(query, 3);
            for (SearchCandidate c : candidates) {
                SearchableChunk chunk = c.getChunk();
                String content = chunk.getContent();
                extractLinesByKeywords(content, List.of("风险", "瓶颈", "冲突", "积压", "失败", "超卖", "挂起"), 
                        line -> bottlenecks.add("[知识库诊断] " + chunk.getTitle() + ": " + line));
                extractLinesByKeywords(content, List.of("优化", "建议", "排查", "配置", "调整"), 
                        line -> suggestions.add("[架构建议] 来自 " + chunk.getTitle() + ": " + line));
            }
        } catch (Exception e) { log.warn("Deep augmentation failed: {}", e.getMessage()); }
    }

    private void extractLinesByKeywords(String content, List<String> keywords, java.util.function.Consumer<String> action) {
        if (content == null) return;
        String[] lines = content.split("\n");
        int count = 0;
        for (String line : lines) {
            String cleanLine = line.trim();
            if (cleanLine.startsWith("#") || cleanLine.length() < 8) continue;
            if (cleanLine.contains("避免") && cleanLine.contains("瓶颈") && cleanLine.contains("成功")) continue;
            for (String kw : keywords) {
                if (cleanLine.toLowerCase().contains(kw.toLowerCase())) {
                    action.accept(cleanLine.replaceAll("^[-*]\\s*", "").trim());
                    count++;
                    break;
                }
            }
            if (count >= 2) break;
        }
    }

    private void generateEmergencySuggestions(AnalyzeSceneType sceneType, List<String> suggestions) {
        switch (sceneType) {
            case SECKILL:
                suggestions.add("[系统预设] 请检查 Redis 中秒杀券库存是否预热，并确认 Lua 脚本执行期间是否有长时间阻塞");
                suggestions.add("[系统预设] 检查 RabbitMQ 消费者 log，确认 VoucherOrderServiceImpl.createVoucherOrder 异步落库是否出现乐观锁冲突");
                suggestions.add("[系统预设] 建议通过 Kibana 或日志排查‘一人一单’拦截器是否在高并发下产生了非预期的重复判定");
                break;
            case SHOP_READ:
                suggestions.add("[系统预设] 请检查 Redis 序列化耗时，并确认 CacheClient 的异步重建线程池是否已满");
                break;
            default:
                suggestions.add("[系统预设] 检查应用服务器最大线程数设置，并关注数据库连接池 (HikariCP) 等待情况");
        }
    }

    private String performBasicAssessment(ParsedPerformanceReport report, List<String> findings) {
        if (report.getErrorRate() > 0.05) return "FAIL";
        if (report.getErrorRate() > 0.01 || report.getAvgElapsed() > 200) return "WARN";
        return "PASS";
    }

    private void analyzeGeneral(ParsedPerformanceReport report, List<String> findings, List<String> suggestions, List<String> bottlenecks) {
        suggestions.add("检查基础架构线程池与连接池配置");
    }

    private void buildSeckillPlan(PressurePlanResponse.PressurePlanResponseBuilder builder) {
        builder.summary("秒杀下单属于极高并发写场景，核心在于 Redis 预减库存与 MQ 异步持久化的解耦能力。")
                .scenarios(List.of("瞬时高并发抢购测试", "MQ 削峰填谷平滑度测试", "库存最终一致性校验"))
                .metrics(List.of("接口 RT (99线 < 100ms)", "系统最高 TPS 承载", "MQ 消息积压速率"))
                .risks(List.of("Redis Lua 脚本性能瓶颈", "数据库乐观锁高频冲突导致回滚", "MQ 消费者处理过慢导致堆积"))
                .prerequisites(List.of("库存预热至 Redis", "压测 Token 集准备"))
                .relatedComponents(List.of("VoucherOrderServiceImpl", "seckill.lua", "RabbitMQ", "Redis"));
    }

    private void buildShopReadPlan(PressurePlanResponse.PressurePlanResponseBuilder builder) {
        builder.summary("商铺查询是典型高频读场景，核心依赖多级缓存架构分担数据库压力。")
                .scenarios(List.of("热点 Key 持续高频访问", "缓存失效瞬间的击穿测试"))
                .metrics(List.of("缓存命中率 (>95%)", "平均响应时间稳定性"))
                .risks(List.of("热点缓存击穿", "大量无效 ID 导致的穿透"))
                .prerequisites(List.of("缓存数据预填充", "监控系统就绪"))
                .relatedComponents(List.of("ShopServiceImpl", "CacheClient", "Caffeine", "Redis"));
    }

    private void buildLoginPlan(PressurePlanResponse.PressurePlanResponseBuilder builder) {
        builder.summary("登录鉴权链路是系统的入口屏障，涉及验证码生成与 Token 存储密集操作。")
                .scenarios(List.of("验证码高频请求并发", "Token 刷新拦截器性能损耗压测"))
                .metrics(List.of("验证码接口 TPS", "Redis 写入 IOPS"))
                .risks(List.of("Redis 写过载", "连接池资源耗尽"))
                .prerequisites(List.of("测试手机号段准备", "Redis 扩容检查"))
                .relatedComponents(List.of("UserServiceImpl", "RefreshTokenInterceptor", "Redis"));
    }

    private void buildGeneralPlan(PressurePlanResponse.PressurePlanResponseBuilder builder) {
        builder.summary("常规业务接口压力测试方案。")
                .scenarios(List.of("基准性能测试", "最大并发承载测试"))
                .metrics(List.of("TPS/RT/ErrorRate"))
                .risks(List.of("数据库连接池溢出", "慢 SQL 导致阻塞"))
                .prerequisites(List.of("基础环境准备"))
                .relatedComponents(List.of("Spring Boot", "MySQL"));
    }

    private void augmentPlanWithKnowledge(PressurePlanResponse.PressurePlanResponseBuilder builder, String endpoint) {
        try {
            List<SearchCandidate> candidates = searchService.search("针对 " + endpoint + " 的压测风险和前置准备", 2);
            if (!candidates.isEmpty()) {
                log.info("Knowledge augmented for endpoint {}: {} items", endpoint, candidates.size());
            }
        } catch (Exception e) { log.warn("Augmentation ignored: {}", e.getMessage()); }
    }

    private String getSceneDisplayName(AnalyzeSceneType type) {
        return switch (type) {
            case SECKILL -> "秒杀抢购";
            case SHOP_READ -> "商铺查询";
            case LOGIN -> "登录鉴权";
            default -> "通用业务";
        };
    }
}
