package com.lifeservice.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agent.llm")
public class LlmProperties {
    private Boolean enabled = false;
    private String provider;
    private String model;
    private String baseUrl;
    private String apiKey;
}
