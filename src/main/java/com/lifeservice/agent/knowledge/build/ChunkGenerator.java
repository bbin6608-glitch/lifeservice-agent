package com.lifeservice.agent.knowledge.build;

import com.lifeservice.agent.knowledge.model.KnowledgeChunk;
import com.lifeservice.agent.knowledge.model.KnowledgeDocument;
import java.util.List;

public interface ChunkGenerator {
    List<KnowledgeChunk> chunk(List<KnowledgeDocument> docs);
}
