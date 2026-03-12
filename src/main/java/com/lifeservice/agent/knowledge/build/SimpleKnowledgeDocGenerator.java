package com.lifeservice.agent.knowledge.build;

import com.lifeservice.agent.knowledge.model.KnowledgeDocument;
import com.lifeservice.agent.knowledge.model.KnowledgeFact;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimpleKnowledgeDocGenerator implements KnowledgeDocGenerator {

    @Override
    public List<KnowledgeDocument> generate(List<KnowledgeFact> facts) {
        return facts.stream().map(fact -> {
            String docId = fact.getTitle();
            if (docId.contains(".")) {
                docId = docId.substring(0, docId.lastIndexOf("."));
            }
            
            // 自动判断类型：如果来源文件路径包含 implementation 或是在 curated 目录下
            boolean isImplementation = fact.getSourceFile() != null && 
                (fact.getSourceFile().contains("implementation") || fact.getSourceFile().contains("curated"));
            
            return KnowledgeDocument.builder()
                    .docId(docId)
                    .title(fact.getTitle())
                    .category(isImplementation ? "implementation" : "raw-import")
                    .knowledgeType(isImplementation ? "IMPLEMENTATION" : "CODE")
                    .sourceMode(isImplementation ? "MANUAL" : "AUTO")
                    .usageScope(List.of("qa", "ptest"))
                    .sourceFiles(List.of(fact.getSourceFile()))
                    .content(fact.getContent())
                    .confidenceLevel(isImplementation ? 1.0 : 0.8)
                    .build();
        }).collect(Collectors.toList());
    }
}
