package com.lifeservice.agent.knowledge.parser;

import com.lifeservice.agent.knowledge.build.RawSourceParser;
import com.lifeservice.agent.knowledge.model.KnowledgeFact;
import com.lifeservice.agent.knowledge.model.RawSourceFile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DefaultRawSourceParser implements RawSourceParser {

    @Override
    public boolean supports(String fileName) {
        return true;
    }

    @Override
    public List<KnowledgeFact> parse(RawSourceFile file) {
        return Collections.singletonList(KnowledgeFact.builder()
                .type("raw")
                .title(file.getFileName())
                .content(file.getContent())
                .tags(Collections.emptyList())
                .sourceFile(file.getRelativePath())
                .build());
    }
}
