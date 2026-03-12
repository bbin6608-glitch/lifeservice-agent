package com.lifeservice.agent.knowledge.build;

import com.lifeservice.agent.knowledge.model.KnowledgeFact;
import com.lifeservice.agent.knowledge.model.RawSourceFile;
import java.util.List;

public interface RawSourceParser {
    boolean supports(String fileName);
    List<KnowledgeFact> parse(RawSourceFile file);
}
