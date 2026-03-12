package com.lifeservice.agent.knowledge.build;

import com.lifeservice.agent.knowledge.model.KnowledgeDocument;
import com.lifeservice.agent.knowledge.model.KnowledgeFact;
import java.util.List;

public interface KnowledgeDocGenerator {
    List<KnowledgeDocument> generate(List<KnowledgeFact> facts);
}
