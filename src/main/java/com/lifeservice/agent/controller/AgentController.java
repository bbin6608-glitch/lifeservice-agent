package com.lifeservice.agent.controller;

import com.lifeservice.agent.dto.agent.AgentChatRequest;
import com.lifeservice.agent.dto.agent.AgentChatResponse;
import com.lifeservice.agent.service.AgentGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Agent 统一入口")
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentGatewayService agentGatewayService;

    @Operation(summary = "Agent 统一对话接口 (智能分发)")
    @PostMapping("/chat")
    public AgentChatResponse chat(@RequestBody AgentChatRequest request) {
        return agentGatewayService.chat(request.getInput());
    }
}
