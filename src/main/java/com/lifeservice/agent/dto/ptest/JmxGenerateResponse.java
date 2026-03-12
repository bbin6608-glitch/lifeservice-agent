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
public class JmxGenerateResponse {
    private String fileName;
    private String jmxContent;
    private PressureSceneType sceneType;
    private String summary;
    private List<String> notes;
}
