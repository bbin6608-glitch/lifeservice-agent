package com.lifeservice.agent.ptest.planner;

import org.springframework.stereotype.Component;

@Component
public class EndpointSceneClassifier {

    public PressureSceneType classify(String endpoint) {
        if (endpoint == null) return PressureSceneType.GENERAL;
        
        if (endpoint.contains("/voucher-order/seckill")) {
            return PressureSceneType.SECKILL;
        }
        if (endpoint.contains("/shop/")) {
            return PressureSceneType.SHOP_READ;
        }
        if (endpoint.contains("/user/code") || endpoint.contains("/user/login")) {
            return PressureSceneType.LOGIN;
        }
        
        return PressureSceneType.GENERAL;
    }
}
