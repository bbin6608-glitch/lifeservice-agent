package com.lifeservice.agent.ptest.jmeter;

import com.lifeservice.agent.config.AgentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class JmxFileWriter {

    public String write(String fileName, String content) {
        try {
            // 固定输出到 pressure-test/jmx 目录
            Path jmxDir = Paths.get("pressure-test", "jmx");
            if (!Files.exists(jmxDir)) {
                Files.createDirectories(jmxDir);
            }

            Path filePath = jmxDir.resolve(fileName);
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
            log.info("Successfully wrote JMX script to: {}", filePath.toAbsolutePath());
            return filePath.toString();
        } catch (IOException e) {
            log.error("Failed to write JMX file: {}", e.getMessage());
            return null;
        }
    }
}
