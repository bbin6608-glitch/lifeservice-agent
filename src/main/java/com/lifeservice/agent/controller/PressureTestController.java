package com.lifeservice.agent.controller;

import com.lifeservice.agent.dto.ptest.*;
import com.lifeservice.agent.infra.persistence.entity.AgentTaskEntity;
import com.lifeservice.agent.infra.repository.AgentTaskRepository;
import com.lifeservice.agent.ptest.service.PressureTestService;
import com.lifeservice.agent.ptest.service.impl.PtestWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "压力测试辅助")
@RestController
@RequestMapping("/api/ptest")
@RequiredArgsConstructor
public class PressureTestController {

    private final PressureTestService pressureTestService;
    private final PtestWorkflowService workflowService;
    private final AgentTaskRepository taskRepository;

    @Operation(summary = "生成压测方案")
    @PostMapping("/plan")
    public PressurePlanResponse generatePlan(@Valid @RequestBody PressurePlanRequest request) {
        return pressureTestService.generatePlan(request);
    }

    @Operation(summary = "生成 JMX 脚本")
    @PostMapping("/jmx")
    public JmxGenerateResponse generateJmx(@RequestBody JmxGenerateRequest request) {
        return pressureTestService.generateJmx(request);
    }

    @Operation(summary = "分析压测结果")
    @PostMapping("/analyze")
    public ResultAnalyzeResponse analyze(@RequestBody ResultAnalyzeRequest request) {
        return pressureTestService.analyze(request);
    }

    // --- 执行型 Agent 任务接口 ---

    @Operation(summary = "创建并启动压测任务 (Plan + JMX)")
    @PostMapping("/task/start")
    public Map<String, Object> startTask(@RequestParam String endpoint) {
        String taskId = workflowService.createPtestTask(endpoint);
        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("status", "WAITING_CONFIRMATION");
        response.put("message", "压测脚本已生成，请在本地运行后上传结果路径执行 complete 接口。");
        return response;
    }

    @Operation(summary = "提交压测结果并完成分析任务")
    @PostMapping("/task/complete")
    public ResultAnalyzeResponse completeTask(@RequestParam String taskId, @RequestParam String reportPath) {
        return workflowService.completeTask(taskId, reportPath);
    }

    @Operation(summary = "获取任务详情与当前进度")
    @GetMapping("/task/{taskId}")
    public AgentTaskEntity getTaskDetail(@PathVariable String taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }
}
