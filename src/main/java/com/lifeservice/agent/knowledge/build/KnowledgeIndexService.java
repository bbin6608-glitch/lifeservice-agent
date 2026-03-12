package com.lifeservice.agent.knowledge.build;

import com.lifeservice.agent.knowledge.model.KnowledgeChunk;
import java.util.List;

public interface KnowledgeIndexService {
    void rebuild(List<KnowledgeChunk> chunks);
}
