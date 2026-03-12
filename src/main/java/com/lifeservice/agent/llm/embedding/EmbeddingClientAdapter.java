package com.lifeservice.agent.llm.embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingClientAdapter {

    private final EmbeddingModel embeddingModel;

    public List<Double> embed(String text) {
        try {
            float[] result = embeddingModel.embed(text);
            if (result == null) return null;
            
            List<Double> list = new ArrayList<>(result.length);
            for (float f : result) {
                list.add((double) f);
            }
            return list;
        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage());
            return null;
        }
    }

    public List<List<Double>> embedBatch(List<String> texts) {
        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(texts);
            if (response == null || response.getResults() == null) return null;
            
            return response.getResults().stream()
                    .map(r -> {
                        float[] fArray = r.getOutput();
                        List<Double> dList = new ArrayList<>(fArray.length);
                        for (float f : fArray) {
                            dList.add((double) f);
                        }
                        return dList;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to generate batch embeddings: {}", e.getMessage());
            return null;
        }
    }
}
