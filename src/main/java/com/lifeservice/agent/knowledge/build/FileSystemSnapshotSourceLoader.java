package com.lifeservice.agent.knowledge.build;

import com.lifeservice.agent.config.KnowledgeBaseProperties;
import com.lifeservice.agent.knowledge.model.RawSourceFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileSystemSnapshotSourceLoader implements SnapshotSourceLoader {

    private final KnowledgeBaseProperties kbProperties;

    @Override
    public List<RawSourceFile> loadAll() {
        List<RawSourceFile> results = new ArrayList<>();
        
        // 1. Scan Raw Path (e.g. project snapshots)
        File rawRoot = new File(kbProperties.getRawPath());
        if (rawRoot.exists() && rawRoot.isDirectory()) {
            walk(rawRoot, rawRoot, results);
        } else {
            log.warn("Raw path does not exist: {}", kbProperties.getRawPath());
        }

        // 2. Scan Curated Path (e.g. implementation experience documents)
        File curatedRoot = new File(kbProperties.getCuratedPath());
        if (curatedRoot.exists() && curatedRoot.isDirectory()) {
            walk(curatedRoot, curatedRoot, results);
        } else {
            log.warn("Curated path does not exist: {}", kbProperties.getCuratedPath());
        }

        return results;
    }

    private void walk(File root, File current, List<RawSourceFile> results) {
        File[] files = current.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                walk(root, f, results);
            } else {
                try {
                    String relativePath = root.toURI().relativize(f.toURI()).getPath();
                    String content = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                    results.add(RawSourceFile.builder()
                            .fileName(f.getName())
                            .relativePath(relativePath)
                            .content(content)
                            .build());
                } catch (Exception e) {
                    log.error("Failed to read file: {}", f.getAbsolutePath(), e);
                }
            }
        }
    }
}
