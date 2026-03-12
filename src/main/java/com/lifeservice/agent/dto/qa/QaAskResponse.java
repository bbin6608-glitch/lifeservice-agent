package com.lifeservice.agent.dto.qa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QaAskResponse {
    private String answer;
    private List<Citation> citations;
    private List<Hit> hits;
    private String answerMode; // local, rag
    private Boolean degradedToLocal;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Hit {
        private String chunkId;
        private String title;
        private String contentSummary;
        
        // 增强的可观测调试字段
        private String engine;        // bm25, vector, hybrid
        private Double bm25Score;
        private Double vectorScore;
        private Double rerankScore;
        private Double finalScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Citation {
        private String docId;
        private String title;
        private String section;
        private List<String> sourceFiles;
    }
}
