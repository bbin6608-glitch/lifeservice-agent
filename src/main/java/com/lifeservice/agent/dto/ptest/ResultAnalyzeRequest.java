package com.lifeservice.agent.dto.ptest;

import lombok.Data;

@Data
public class ResultAnalyzeRequest {
    private String endpoint;
    private String reportFilePath;
}
