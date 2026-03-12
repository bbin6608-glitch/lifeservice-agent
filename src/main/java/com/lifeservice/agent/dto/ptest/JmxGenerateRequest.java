package com.lifeservice.agent.dto.ptest;

import lombok.Data;
import java.util.Map;

@Data
public class JmxGenerateRequest {
    private String endpoint;
    private String scenarioType;
    private Map<String, Object> variables;
}
