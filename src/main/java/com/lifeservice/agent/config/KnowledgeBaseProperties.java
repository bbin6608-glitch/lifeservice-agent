package com.lifeservice.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agent.kb")
public class KnowledgeBaseProperties {
    private String rawPath;
    private String curatedPath;
    private String chunkPath;
    private String indexPath;
}