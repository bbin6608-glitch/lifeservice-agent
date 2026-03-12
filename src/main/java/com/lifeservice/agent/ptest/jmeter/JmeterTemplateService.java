package com.lifeservice.agent.ptest.jmeter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JmeterTemplateService {

    private final FreemarkerTemplateRenderer renderer;

    public String generate(String endpoint, JmeterTemplateType type, Map<String, Object> customVars) {
        Map<String, Object> model = new HashMap<>();
        
        // 1. 设置默认基础变量
        model.put("host", "localhost");
        model.put("port", "8081");
        model.put("threads", "1000");
        model.put("rampUp", "1");
        model.put("duration", "60");
        
        // 2. 根据场景设置特定默认值
        if (type == JmeterTemplateType.SECKILL) {
            model.put("voucherId", "11");
            model.put("tokenFile", "pressure-test/data/tokens.txt");
            model.put("resultFile", "pressure-test/results/seckill-results.jtl");
        }

        // 3. 应用外部传入的自定义变量
        if (customVars != null) {
            model.putAll(customVars);
        }

        // 4. 特殊逻辑：从 endpoint 中提取 ID (如果用户没传 voucherId)
        if (type == JmeterTemplateType.SECKILL && !model.containsKey("voucherId") && endpoint != null) {
            String extractedId = extractIdFromEndpoint(endpoint);
            if (extractedId != null) model.put("voucherId", extractedId);
        }

        // 5. 渲染
        String templateName = getTemplateName(type);
        return renderer.render(templateName, model);
    }

    private String getTemplateName(JmeterTemplateType type) {
        return switch (type) {
            case SECKILL -> "jmeter/seckill-test-plan.jmx.ftl";
            case SHOP_READ -> "jmeter/shop-read-test-plan.jmx.ftl";
            case LOGIN -> "jmeter/login-test-plan.jmx.ftl";
            default -> "jmeter/general-test-plan.jmx.ftl";
        };
    }

    private String extractIdFromEndpoint(String endpoint) {
        if (endpoint == null) return null;
        String[] parts = endpoint.split("/");
        for (String part : parts) {
            if (part.matches("\\d+")) return part;
        }
        return null;
    }
}
