package com.lifeservice.agent.qa.service.impl;

import com.lifeservice.agent.dto.qa.QaAskRequest;
import com.lifeservice.agent.dto.qa.QaAskResponse;
import com.lifeservice.agent.knowledge.model.SearchCandidate;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import com.lifeservice.agent.knowledge.retrieve.KnowledgeSearchService;
import com.lifeservice.agent.knowledge.retrieve.SourceMapLoader;
import com.lifeservice.agent.qa.router.QuestionTopicClassifier;
import com.lifeservice.agent.qa.service.QaAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 本地 QA 代理服务 - 修复协议一致性与内容噪音
 */
@Slf4j
@Service("localModeQaAgentService")
@RequiredArgsConstructor
public class LocalModeQaAgentService implements QaAgentService {

    private final KnowledgeSearchService searchService;
    private final QuestionTopicClassifier topicClassifier;
    private final SourceMapLoader sourceMapLoader;

    private static final Pattern COMPONENT_PATTERN = Pattern.compile("`([a-zA-Z0-9._-]+)`|[A-Z][a-zA-Z0-9]*(?:ServiceImpl|Runner|Worker|Controller|Mapper|Config|Client|Handler|Service)");
    private static final String[] MIDDLEWARE_KEYWORDS = {"Redis", "RabbitMQ", "Lua", "MySQL", "Canal", "Caffeine", "MyBatis"};
    private static final Pattern FRONT_MATTER_PATTERN = Pattern.compile("^---[\\s\\S]*?---", Pattern.MULTILINE);

    @Override
    public QaAskResponse ask(QaAskRequest request) {
        int topK = request.getTopK() != null ? request.getTopK() : 5;
        List<SearchCandidate> candidates;
        try {
            candidates = searchService.search(request.getQuestion(), topK);
        } catch (Exception e) {
            log.error("Search failed: {}", e.getMessage());
            return buildErrorResponse("检索失败。");
        }

        if (candidates == null || candidates.isEmpty()) {
            return buildErrorResponse("知识库未命中相关内容。");
        }

        List<SearchableChunk> hits = candidates.stream()
                .map(SearchCandidate::getChunk)
                .collect(Collectors.toList());

        QuestionTopicClassifier.Topic topic = topicClassifier.classify(request.getQuestion());
        String answer = buildStructuredAnswer(topic, hits, request.getQuestion());
        
        return buildFinalResponse(answer, candidates, request);
    }

    private String buildStructuredAnswer(QuestionTopicClassifier.Topic topic, List<SearchableChunk> hits, String question) {
        switch (topic) {
            case APP_PROFILE: return buildProfileAnswer(hits);
            case SECKILL_FLOW: return buildSeckillAnswer(hits);
            case SHOP_CACHE: return buildShopCacheAnswer(hits);
            default: return buildGeneralAnswer(hits);
        }
    }

    private String buildProfileAnswer(List<SearchableChunk> hits) {
        StringBuilder sb = new StringBuilder();
        sb.append("【App1 与 App2 运行差异深度解析】\n\n");

        addSlot(sb, "- 核心结论：", extractExactContent(hits, "一句话结论"));
        addSlot(sb, "- 背景定位：", extractExactContent(hits, "背景", "定位"));
        addSlot(sb, "- App1 特点：", extractExactContent(hits, "App1 特点", "App1特点"));
        addSlot(sb, "- App2 特点：", extractExactContent(hits, "App2 特点", "App2特点"));
        
        String diffs = extractExactContent(hits, "差异总结");
        if (!diffs.isEmpty()) sb.append("- 核心差异点：\n").append(formatLongContent(diffs, "  ")).append("\n");

        addSlot(sb, "- 关键风险：", extractExactContent(hits, "风险点", "风险提示"));
        addSlot(sb, "- 验证方式：", extractExactContent(hits, "验证方式", "测试方法"));

        appendSuggestions(sb, List.of("App1/App2 端口配置", "本地缓存一致性风险"));
        return sb.toString();
    }

    private String buildSeckillAnswer(List<SearchableChunk> hits) {
        StringBuilder sb = new StringBuilder();
        sb.append("【秒杀下单链路专题回答】\n\n");
        addSlot(sb, "- 一句话结论：", extractExactContent(hits, "一句话结论"));
        sb.append("- 核心组件：").append(extractComponents(hits)).append("\n");
        sb.append("- 执行步骤：\n").append(extractOrderedSteps(hits)).append("\n");
        addSlot(sb, "- 一致性保证：", extractExactContent(hits, "一致性", "数据校验"));
        appendSuggestions(sb, List.of("Redis Lua 脚本细节", "RabbitMQ 削峰机制"));
        return sb.toString();
    }

    private String buildShopCacheAnswer(List<SearchableChunk> hits) {
        StringBuilder sb = new StringBuilder();
        sb.append("【商铺缓存策略解析】\n\n");
        addSlot(sb, "- 核心逻辑：", extractExactContent(hits, "一句话结论", "机制说明"));
        addSlot(sb, "- 缓存机制：", extractExactContent(hits, "特点", "缓存机制", "策略"));
        addSlot(sb, "- 风险与应对：", extractExactContent(hits, "风险点", "缓存穿透", "击穿", "风险"));
        appendSuggestions(sb, List.of("CacheClient 逻辑过期实现", "Caffeine 本地缓存配置"));
        return sb.toString();
    }

    private String buildGeneralAnswer(List<SearchableChunk> hits) {
        StringBuilder sb = new StringBuilder();
        sb.append("根据知识库检索，相关核心事实如下：\n\n");
        int count = 0;
        for (SearchableChunk chunk : hits) {
            String content = cleanMarkdownContent(chunk.getContent());
            if (content.isEmpty()) continue;
            count++;
            sb.append(count).append(". ").append(chunk.getSection()).append("：")
              .append(content).append("\n\n");
            if (count >= 3) break;
        }
        return sb.toString();
    }

    // --- 工具方法 ---

    private void addSlot(StringBuilder sb, String label, String content) {
        if (content != null && !content.isEmpty()) {
            sb.append(label).append(content).append("\n");
        }
    }

    private String extractExactContent(List<SearchableChunk> hits, String... sectionKeywords) {
        for (SearchableChunk chunk : hits) {
            String section = chunk.getSection();
            if (section == null) continue;
            for (String kw : sectionKeywords) {
                if (section.equalsIgnoreCase(kw) || section.contains(kw)) {
                    return cleanMarkdownContent(chunk.getContent());
                }
            }
        }
        return "";
    }

    private String cleanMarkdownContent(String content) {
        if (content == null) return "";
        // 1. 移除 YAML Front Matter
        String clean = FRONT_MATTER_PATTERN.matcher(content).replaceAll("");
        // 2. 移除 Markdown 标题行 (e.g., "## 标题")
        clean = clean.replaceAll("(?m)^#+\\s+.*$", "");
        // 3. 移除多余空白
        clean = clean.replaceAll("\\n+", " ").trim();
        if (clean.length() > 300) clean = clean.substring(0, 297) + "...";
        return clean;
    }

    private String formatLongContent(String content, String indent) {
        if (content == null || content.isEmpty()) return "";
        String clean = FRONT_MATTER_PATTERN.matcher(content).replaceAll("").trim();
        return Arrays.stream(clean.split("\n"))
                .filter(line -> !line.startsWith("#"))
                .map(line -> indent + line)
                .collect(Collectors.joining("\n"));
    }

    private String extractComponents(List<SearchableChunk> hits) {
        Set<String> components = new LinkedHashSet<>();
        String allContent = hits.stream().map(h -> h.getContent() != null ? h.getContent() : "").collect(Collectors.joining(" "));
        for (String kw : MIDDLEWARE_KEYWORDS) {
            if (allContent.toLowerCase().contains(kw.toLowerCase())) components.add(kw);
        }
        return components.stream().limit(6).collect(Collectors.joining(", "));
    }

    private String extractOrderedSteps(List<SearchableChunk> hits) {
        for (SearchableChunk chunk : hits) {
            if (chunk.getSection() != null && (chunk.getSection().contains("步骤") || chunk.getSection().contains("链路"))) {
                return Arrays.stream(chunk.getContent().split("\n"))
                        .filter(line -> line.matches("^\\d+\\..*") || line.startsWith("-") || line.startsWith("*"))
                        .map(line -> "   " + line)
                        .limit(6)
                        .collect(Collectors.joining("\n"));
            }
        }
        return "   (见引文详细说明)";
    }

    private QaAskResponse buildFinalResponse(String answer, List<SearchCandidate> candidates, QaAskRequest request) {
        List<SearchableChunk> hits = candidates.stream().map(SearchCandidate::getChunk).collect(Collectors.toList());
        
        // 构建 Citations
        List<QaAskResponse.Citation> citations = new ArrayList<>();
        Map<String, QaAskResponse.Citation> citationMap = new LinkedHashMap<>();
        for (SearchableChunk chunk : hits) {
            String citeKey = (chunk.getDocId() != null ? chunk.getDocId() : "unknown") + "::" + (chunk.getSection() != null ? chunk.getSection() : "正文");
            if (!citationMap.containsKey(citeKey)) {
                citationMap.put(citeKey, QaAskResponse.Citation.builder()
                        .docId(chunk.getDocId())
                        .title(chunk.getTitle())
                        .section(chunk.getSection())
                        .sourceFiles(chunk.getSourceFiles() != null ? chunk.getSourceFiles() : new ArrayList<>())
                        .build());
            }
        }
        citations.addAll(citationMap.values());

        return QaAskResponse.builder()
                .answer(answer)
                .hits(candidates.stream().limit(5).map(c -> {
                    SearchableChunk h = c.getChunk();
                    return QaAskResponse.Hit.builder()
                        .chunkId(h.getChunkId())
                        .title(h.getTitle())
                        .contentSummary(cleanMarkdownContent(h.getContent()))
                        .engine(c.getEngine())
                        .bm25Score(c.getBm25Score())
                        .vectorScore(c.getVectorScore())
                        .rerankScore(c.getRerankScore())
                        .finalScore(c.getScore())
                        .build();
                }).collect(Collectors.toList()))
                .citations(citations)
                .answerMode("local")
                .degradedToLocal(false)
                .build();
    }

    private QaAskResponse buildErrorResponse(String message) {
        return QaAskResponse.builder()
                .answer(message)
                .citations(new ArrayList<>())
                .hits(new ArrayList<>())
                .answerMode("local")
                .degradedToLocal(true)
                .build();
    }

    private void appendSuggestions(StringBuilder sb, List<String> suggestions) {
        sb.append("\n建议追问：\n");
        for (String s : suggestions) sb.append("- ").append(s).append("\n");
    }
}
