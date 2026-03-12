package com.lifeservice.agent.controller;

import com.lifeservice.agent.mcp.handler.McpToolHandler;
import com.lifeservice.agent.mcp.model.McpToolCallRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "MCP 适配层")
@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpAdapterController {

    private final McpToolHandler toolHandler;

    @Operation(summary = "列出所有可用的 MCP Tools")
    @GetMapping("/tools")
    public Map<String, Object> listTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        tools.add(createTool("qa_ask", "项目知识问答，涵盖架构、源码与压测方案。"));
        tools.add(createTool("kb_sync_db", "将本地知识文件同步至数据库并重建 Lucene 索引。"));
        tools.add(createTool("kb_rebuild", "扫描源码并重新生成本地知识碎片文件 (chunks.jsonl)。"));
        tools.add(createTool("ptest_start_task", "启动一个压测工作流，自动生成方案与 JMX 脚本。"));
        tools.add(createTool("ptest_complete_task", "提交压测结果 JTL 文件并生成最终性能分析报告。"));

        Map<String, Object> response = new HashMap<>();
        response.put("tools", tools);
        return response;
    }

    @Operation(summary = "执行 MCP Tool 调用")
    @PostMapping("/rpc")
    public Object callTool(@RequestBody McpToolCallRequest request) {
        if (!"tools/call".equals(request.getMethod())) {
            throw new IllegalArgumentException("Unsupported method: " + request.getMethod());
        }
        return toolHandler.handle(request.getParams().getName(), request.getParams().getArguments());
    }

    private Map<String, Object> createTool(String name, String description) {
        Map<String, Object> tool = new HashMap<>();
        tool.put("name", name);
        tool.put("description", description);
        return tool;
    }
}
