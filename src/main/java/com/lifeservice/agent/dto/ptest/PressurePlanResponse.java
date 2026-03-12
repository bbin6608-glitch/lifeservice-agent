package com.lifeservice.agent.dto.ptest;

import com.lifeservice.agent.ptest.planner.PressureSceneType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PressurePlanResponse {
    private String endpoint;
    private PressureSceneType sceneType;
    private String summary;
    private List<String> scenarios;
    private List<String> metrics;
    private List<String> risks;
    private List<String> prerequisites;
    private List<String> relatedComponents;
}
