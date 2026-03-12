package com.lifeservice.agent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LifeService Agent API")
                        .version("1.0")
                        .description("用于 local-lifeservice 项目的知识问答与压测辅助接口"));
    }
}
