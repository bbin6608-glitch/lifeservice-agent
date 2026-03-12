package com.lifeservice.agent.knowledge.store;

import com.lifeservice.agent.knowledge.build.KnowledgeIndexService;
import com.lifeservice.agent.knowledge.model.KnowledgeChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NoopKnowledgeIndexService implements KnowledgeIndexService {

    @Override
    public void rebuild(List<KnowledgeChunk> chunks) {
        log.info("Indexing {} chunks (No-op implementation)", chunks.size());
    }
}
