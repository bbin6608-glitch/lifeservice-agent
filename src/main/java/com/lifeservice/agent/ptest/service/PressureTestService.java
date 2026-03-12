package com.lifeservice.agent.ptest.service;

import com.lifeservice.agent.dto.ptest.*;

public interface PressureTestService {
    PressurePlanResponse generatePlan(PressurePlanRequest request);
    JmxGenerateResponse generateJmx(JmxGenerateRequest request);
    ResultAnalyzeResponse analyze(ResultAnalyzeRequest request);
}
