package com.lifeservice.agent.mcp.handler;

import com.lifeservice.agent.dto.qa.QaAskRequest;
import com.lifeservice.agent.knowledge.build.*;
import com.lifeservice.agent.knowledge.model.KnowledgeChunk;
import com.lifeservice.agent.knowledge.model.KnowledgeDocument;
import com.lifeservice.agent.knowledge.model.KnowledgeFact;
import com.lifeservice.agent.knowledge.model.RawSourceFile;
import com.lifeservice.agent.knowledge.store.KnowledgeSyncService;
import com.lifeservice.agent.ptest.service.impl.PtestWorkflowService;
import com.lifeservice.agent.qa.service.impl.QaModeRouterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolHandler {

    private final QaModeRouterService qaService;
    private final KnowledgeSyncService syncService;
    private final PtestWorkflowService ptestWorkflow;
    
    // 用于 kb_rebuild 的原始构建组件
    private final SnapshotSourceLoader sourceLoader;
    private final RawSourceParser sourceParser;
    private final KnowledgeDocGenerator docGenerator;
    private final ChunkGenerator chunkGenerator;
    private final KnowledgeIndexService indexService;

    public Object handle(String toolName, Map<String, Object> args) {
        log.info("MCP Tool Call: {} with args: {}", toolName, args);
        
        return switch (toolName) {
            case "qa_ask" -> handleQaAsk(args);
            case "kb_sync_db" -> syncService.syncAll();
            case "kb_rebuild" -> handleKbRebuild();
            case "ptest_start_task" -> handlePtestStart(args);
            case "ptest_complete_task" -> handlePtestComplete(args);
            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }

    private Object handleQaAsk(Map<String, Object> args) {
        QaAskRequest req = new QaAskRequest();
        req.setQuestion((String) args.get("question"));
        req.setMode((String) args.getOrDefault("mode", "rag"));
        return qaService.ask(req);
    }

    private Object handleKbRebuild() {
        List<RawSourceFile> files = sourceLoader.loadAll();

        List<KnowledgeFact> facts = files.stream()
                .map(sourceParser::parse)
                .flatMap(List::stream)
                .toList();

        List<KnowledgeDocument> docs = docGenerator.generate(facts);
        List<KnowledgeChunk> chunks = chunkGenerator.chunk(docs);
        indexService.rebuild(chunks);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("sourceFiles", files.size());
        res.put("factsGenerated", facts.size());
        res.put("documentsGenerated", docs.size());
        res.put("chunksGenerated", chunks.size());
        return res;
    }
    private Object handlePtestStart(Map<String, Object> args) {
        String endpoint = (String) args.get("endpoint");
        String taskId = ptestWorkflow.createPtestTask(endpoint);
        Map<String, Object> res = new HashMap<>();
        res.put("taskId", taskId);
        res.put("status", "WAITING_CONFIRMATION");
        return res;
    }

    private Object handlePtestComplete(Map<String, Object> args) {
        return ptestWorkflow.completeTask(
                (String) args.get("taskId"), 
                (String) args.get("reportPath")
        );
    }
}
