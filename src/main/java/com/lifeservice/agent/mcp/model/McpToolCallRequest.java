package com.lifeservice.agent.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolCallRequest {
    private String method; // "tools/call"
    private Params params;

    @Data
    public static class Params {
        private String name;
        private Map<String, Object> arguments;
    }
}
