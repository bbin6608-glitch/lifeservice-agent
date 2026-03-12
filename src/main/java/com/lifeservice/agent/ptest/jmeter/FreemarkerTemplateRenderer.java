package com.lifeservice.agent.ptest.jmeter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FreemarkerTemplateRenderer {

    private final Configuration freemarkerConfig;

    public String render(String templateName, Map<String, Object> model) {
        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (Exception e) {
            log.error("Failed to render freemarker template {}: {}", templateName, e.getMessage());
            throw new RuntimeException("JMX template rendering failed", e);
        }
    }
}
