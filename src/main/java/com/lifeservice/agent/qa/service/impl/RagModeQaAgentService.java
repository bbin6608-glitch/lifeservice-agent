package com.lifeservice.agent.qa.service.impl;

import com.lifeservice.agent.dto.qa.QaAskRequest;
import com.lifeservice.agent.dto.qa.QaAskResponse;
import com.lifeservice.agent.knowledge.model.SearchCandidate;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import com.lifeservice.agent.knowledge.retrieve.KnowledgeSearchService;
import com.lifeservice.agent.knowledge.retrieve.SourceMapLoader;
import com.lifeservice.agent.llm.chat.RagChatService;
import com.lifeservice.agent.qa.service.QaAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagModeQaAgentService implements QaAgentService {

    private final KnowledgeSearchService searchService;
    private final RagChatService ragChatService;
    private final SourceMapLoader sourceMapLoader;

    @Override
    public QaAskResponse ask(QaAskRequest request) {
        int topK = request.getTopK() != null ? request.getTopK() : 5;
        List<SearchCandidate> candidates;
        try {
            candidates = searchService.search(request.getQuestion(), topK);
        } catch (Exception e) {
            log.error("Search failed: {}", e.getMessage());
            return QaAskResponse.builder()
                    .answer("知识库检索过程中发生错误。")
                    .citations(new ArrayList<>())
                    .hits(new ArrayList<>())
                    .build();
        }

        if (candidates == null || candidates.isEmpty()) {
            return QaAskResponse.builder()
                    .answer("知识库中未检索到相关内容，无法生成回答。")
                    .citations(new ArrayList<>())
                    .hits(new ArrayList<>())
                    .build();
        }

        List<SearchableChunk> hits = candidates.stream()
                .map(SearchCandidate::getChunk)
                .collect(Collectors.toList());

        String answer;
        try {
            answer = ragChatService.call(request.getQuestion(), hits);
        } catch (Exception e) {
            log.warn("RAG mode failed, please check API key or network: {}", e.getMessage());
            return QaAskResponse.builder()
                    .answer("RAG 模式调用失败，请检查 API Key 配置或网络连接。错误信息: " + e.getMessage())
                    .citations(new ArrayList<>())
                    .hits(new ArrayList<>())
                    .build();
        }

        // Build citations and hits
        Map<String, QaAskResponse.Citation> citationMap = new LinkedHashMap<>();
        List<QaAskResponse.Hit> responseHits = new ArrayList<>();
        Set<String> processedChunkIds = new HashSet<>();

        for (SearchableChunk chunk : hits) {
            String docId = chunk.getDocId() != null ? chunk.getDocId() : "unknown-doc";
            String title = chunk.getTitle() != null ? chunk.getTitle() : docId;
            String section = chunk.getSection() != null ? chunk.getSection() : "文档正文";
            
            List<String> sourceFiles = chunk.getSourceFiles();
            if (sourceFiles == null || sourceFiles.isEmpty()) {
                sourceFiles = sourceMapLoader.getSourceFiles(docId, title);
            }
            if (sourceFiles == null) sourceFiles = new ArrayList<>();
            sourceFiles = sourceFiles.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());

            if (Boolean.TRUE.equals(request.getIncludeCitations())) {
                String citeKey = docId + "::" + section;
                if (!citationMap.containsKey(citeKey)) {
                    citationMap.put(citeKey, QaAskResponse.Citation.builder()
                            .docId(docId)
                            .title(title)
                            .section(section)
                            .sourceFiles(sourceFiles)
                            .build());
                }
            }

            if (chunk.getChunkId() != null && !processedChunkIds.contains(chunk.getChunkId())) {
                String content = chunk.getContent() != null ? chunk.getContent() : "";
                String summary = content.length() > 160 ? content.substring(0, 157) + "..." : content;
                responseHits.add(QaAskResponse.Hit.builder()
                        .chunkId(chunk.getChunkId())
                        .title(title)
                        .contentSummary(summary)
                        .build());
                processedChunkIds.add(chunk.getChunkId());
            }
        }

        return QaAskResponse.builder()
                .answer(answer)
                .citations(new ArrayList<>(citationMap.values()))
                .hits(responseHits)
                .answerMode("rag")
                .degradedToLocal(false)
                .build();
    }
}
