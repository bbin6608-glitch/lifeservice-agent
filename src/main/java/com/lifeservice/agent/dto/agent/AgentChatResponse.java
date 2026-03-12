package com.lifeservice.agent.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatResponse {
    private String input;
    private String selectedTool;
    private Map<String, Object> toolArguments;
    private Object rawToolResult;
    private String finalAnswer;
}
