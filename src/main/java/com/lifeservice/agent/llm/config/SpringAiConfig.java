package com.lifeservice.agent.llm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SpringAiConfig {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("DASHSCOPE_API_KEY is missing. RAG mode will be downgraded to local mode.");
        }
        return builder.build();
    }
}
