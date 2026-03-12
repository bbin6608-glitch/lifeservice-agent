package com.lifeservice.agent.knowledge.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchableChunk {
    private String chunkId;
    private String docId;
    private String title;
    private String category;
    private String section;
    private String content;
    
    // 核心流标识
    @Builder.Default
    private String knowledgeType = "IMPLEMENTATION";
    @Builder.Default
    private String sourceMode = "MANUAL";
    @Builder.Default
    private Double confidenceLevel = 1.0;
    
    private List<String> tags;
    private List<String> scenarioTags;
    private List<String> usageScope;
    
    @JsonAlias({"metadata.sourceFiles", "source_files"})
    private List<String> sourceFiles;
    
    @JsonAlias({"metadata.endpoints", "endpoints"})
    private List<String> endpoints;
    
    @JsonAlias({"metadata.middleware", "middleware"})
    private List<String> middleware;
    
    private Integer priority;

    public boolean isDifferenceContent() {
        if ("差异总结".equals(section)) return true;
        if (content != null && (content.contains("差异") || content.contains("区别") || content.contains("不同"))) return true;
        return false;
    }

    public boolean isHighValueSection() {
        if (section == null) return false;
        return section.contains("一句话结论") || 
               section.contains("差异总结") || 
               section.contains("背景/定位") || 
               section.contains("核心流程") || 
               section.contains("执行步骤") || 
               section.contains("关键配置") || 
               section.contains("来源依据") ||
               section.contains("关键事实");
    }

    public boolean isLowValueSection() {
        if (section == null) return false;
        return section.contains("对问答 agent 的价值") || 
               section.contains("对压测 agent 的价值") || 
               section.contains("建议继续追问") || 
               section.contains("材料不足说明");
    }
}
