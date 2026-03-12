package com.lifeservice.agent.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMetadata {
    private List<String> tags;
    private List<String> scenarioTags;
    private List<String> classes;
    private List<String> methods;
    private List<String> endpoints;
    private List<String> middleware;
    private List<String> profiles;
    private List<String> usageScope;
    private List<String> sourceFiles;
    private String filePath;
    private Integer priority;
}
