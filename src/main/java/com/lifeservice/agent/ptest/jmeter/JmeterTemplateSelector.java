package com.lifeservice.agent.ptest.jmeter;

import org.springframework.stereotype.Component;

@Component
public class JmeterTemplateSelector {

    public JmeterTemplateType select(String endpoint, String scenarioType) {
        if (scenarioType != null && !scenarioType.isEmpty()) {
            try {
                return JmeterTemplateType.valueOf(scenarioType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore and fallback to endpoint matching
            }
        }

        if (endpoint == null || endpoint.isEmpty()) {
            return JmeterTemplateType.GENERAL;
        }

        if (endpoint.contains("/voucher-order/seckill")) {
            return JmeterTemplateType.SECKILL;
        }
        if (endpoint.contains("/shop/")) {
            return JmeterTemplateType.SHOP_READ;
        }
        if (endpoint.contains("/user/code") || endpoint.contains("/user/login")) {
            return JmeterTemplateType.LOGIN;
        }

        return JmeterTemplateType.GENERAL;
    }
}
