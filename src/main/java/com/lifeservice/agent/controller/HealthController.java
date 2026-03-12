package com.lifeservice.agent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "系统健康检查")
@RestController
public class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "app", "lifeservice-agent",
                "status", "ok"
        );
    }
}