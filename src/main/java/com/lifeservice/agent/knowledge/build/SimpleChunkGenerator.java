package com.lifeservice.agent.knowledge.build;

import com.lifeservice.agent.knowledge.model.ChunkMetadata;
import com.lifeservice.agent.knowledge.model.KnowledgeChunk;
import com.lifeservice.agent.knowledge.model.KnowledgeDocument;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimpleChunkGenerator implements ChunkGenerator {

    @Override
    public List<KnowledgeChunk> chunk(List<KnowledgeDocument> docs) {
        return docs.stream().map(doc -> KnowledgeChunk.builder()
                .chunkId(doc.getDocId() + "_full")
                .docId(doc.getDocId())
                .title(doc.getTitle())
                .section("一句话结论") // 默认设置为高价值 section 方便 Rerank 捕捉
                .category(doc.getCategory())
                .knowledgeType(doc.getKnowledgeType())
                .sourceMode(doc.getSourceMode())
                .confidenceLevel(doc.getConfidenceLevel())
                .content(doc.getContent())
                .metadata(ChunkMetadata.builder()
                        .priority(1)
                        .usageScope(doc.getUsageScope())
                        .sourceFiles(doc.getSourceFiles())
                        .build())
                .build()).collect(Collectors.toList());
    }
}
