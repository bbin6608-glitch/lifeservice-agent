package com.lifeservice.agent.ptest.service.impl;

import com.lifeservice.agent.dto.ptest.*;
import com.lifeservice.agent.infra.persistence.entity.*;
import com.lifeservice.agent.infra.repository.*;
import com.lifeservice.agent.ptest.service.PressureTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 压测执行型 Agent 工作流引擎
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PtestWorkflowService {

    private final AgentTaskRepository taskRepository;
    private final AgentTaskStepRepository stepRepository;
    private final AgentTaskEventRepository eventRepository;
    private final PressureTestService ptestService;

    /**
     * 初始化一个压测任务：自动执行 Plan + JMX 步骤
     */
    @Transactional
    public String createPtestTask(String endpoint) {
        String taskId = UUID.randomUUID().toString();
        
        // 1. 创建任务主记录
        AgentTaskEntity task = AgentTaskEntity.builder()
                .taskId(taskId).type("PTEST").endpoint(endpoint)
                .status("RUNNING").currentStep("PLAN").createdAt(LocalDateTime.now()).build();
        taskRepository.save(task);

        addEvent(taskId, "INFO", "TASK_START", "开始压测任务: " + endpoint);

        // 2. 自动执行步骤：PLAN
        executePlanStep(task);

        // 3. 自动执行步骤：JMX
        executeJmxStep(task);

        // 4. 更新状态至等待确认 (等待用户执行压测并上传结果)
        task.setStatus("WAITING_CONFIRMATION");
        task.setCurrentStep("EXECUTE");
        taskRepository.save(task);
        
        addEvent(taskId, "INFO", "WAIT_USER", "压测脚本已生成，请在本地运行并上传结果文件。");
        
        return taskId;
    }

    /**
     * 完成任务：执行 Analyze 步骤
     */
    @Transactional
    public ResultAnalyzeResponse completeTask(String taskId, String reportFilePath) {
        AgentTaskEntity task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus("RUNNING");
        task.setCurrentStep("ANALYZE");
        taskRepository.save(task);

        addEvent(taskId, "INFO", "ANALYZE_START", "开始分析压测结果: " + reportFilePath);

        ResultAnalyzeRequest request = new ResultAnalyzeRequest();
        request.setEndpoint(task.getEndpoint());
        request.setReportFilePath(reportFilePath);
        
        ResultAnalyzeResponse response = ptestService.analyze(request);

        // 保存分析步骤
        stepRepository.save(AgentTaskStepEntity.builder()
                .taskId(taskId).stepName("ANALYZE").status("SUCCESS")
                .ordinal(3).endTime(LocalDateTime.now()).build());

        task.setStatus("SUCCESS");
        task.setResultSummary(response.getSummary());
        taskRepository.save(task);

        addEvent(taskId, "INFO", "TASK_COMPLETE", "任务完成，评定结果: " + response.getAssessment());
        
        return response;
    }

    private void executePlanStep(AgentTaskEntity task) {
        PressurePlanRequest req = new PressurePlanRequest();
        req.setEndpoint(task.getEndpoint());
        ptestService.generatePlan(req);
        
        stepRepository.save(AgentTaskStepEntity.builder()
                .taskId(task.getTaskId()).stepName("PLAN").status("SUCCESS")
                .ordinal(1).startTime(LocalDateTime.now()).endTime(LocalDateTime.now()).build());
        addEvent(task.getTaskId(), "INFO", "STEP_DONE", "压测计划生成完成");
    }

    private void executeJmxStep(AgentTaskEntity task) {
        JmxGenerateRequest req = new JmxGenerateRequest();
        req.setEndpoint(task.getEndpoint());
        ptestService.generateJmx(req);

        stepRepository.save(AgentTaskStepEntity.builder()
                .taskId(task.getTaskId()).stepName("JMX").status("SUCCESS")
                .ordinal(2).startTime(LocalDateTime.now()).endTime(LocalDateTime.now()).build());
        addEvent(task.getTaskId(), "INFO", "STEP_DONE", "JMX 脚本生成完成");
    }

    private void addEvent(String taskId, String level, String type, String msg) {
        eventRepository.save(AgentTaskEventEntity.builder()
                .taskId(taskId).level(level).eventType(type).message(msg)
                .timestamp(LocalDateTime.now()).build());
    }
}
