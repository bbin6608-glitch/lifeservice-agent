package com.lifeservice.agent.ptest.analyzer;

import org.springframework.stereotype.Component;

@Component
public class AnalyzeSceneClassifier {

    public AnalyzeSceneType classify(String endpoint) {
        if (endpoint == null) return AnalyzeSceneType.GENERAL;
        
        if (endpoint.contains("/voucher-order/seckill")) {
            return AnalyzeSceneType.SECKILL;
        }
        if (endpoint.contains("/shop/")) {
            return AnalyzeSceneType.SHOP_READ;
        }
        if (endpoint.contains("/user/code") || endpoint.contains("/user/login")) {
            return AnalyzeSceneType.LOGIN;
        }
        
        return AnalyzeSceneType.GENERAL;
    }
}
