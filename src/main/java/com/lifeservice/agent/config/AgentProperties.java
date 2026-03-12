package com.lifeservice.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {
    
    private AppProperties app = new AppProperties();
    private QaProperties qa = new QaProperties();

    @Data
    public static class AppProperties {
        private String name = "lifeservice-agent";
        private String version = "v1";
    }

    @Data
    public static class QaProperties {
        /**
         * 默认问答模式: local, rag
         */
        private String mode = "local";
    }
}
