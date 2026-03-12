package com.lifeservice.agent.ptest.report;

import com.lifeservice.agent.ptest.analyzer.AnalyzeSceneType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class PerformanceReportFileWriter {

    private static final String RESULTS_DIR = "pressure-test/results";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public FileWriteResult write(String endpoint, AnalyzeSceneType sceneType, String markdownReport) {
        if (markdownReport == null || markdownReport.isEmpty()) {
            return null;
        }

        // 1. Ensure directory exists
        File dir = new File(RESULTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. Generate filename
        String typeStr = mapSceneType(sceneType);
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String fileName = String.format("analysis-%s-%s.md", typeStr, timestamp);
        File file = new File(dir, fileName);

        // 3. Prepare content with metadata footer
        StringBuilder content = new StringBuilder(markdownReport);
        content.append("\n---\n");
        content.append("## 元数据\n");
        content.append("- **生成时间**: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        content.append("- **压测端点**: `").append(endpoint).append("`\n");
        content.append("- **场景标识**: ").append(sceneType).append("\n");

        // 4. Write file
        try {
            Files.write(file.toPath(), content.toString().getBytes(StandardCharsets.UTF_8));
            log.info("Performance report saved to: {}", file.getAbsolutePath());
            return new FileWriteResult(fileName, file.getPath());
        } catch (IOException e) {
            log.error("Failed to save performance report file: {}", e.getMessage(), e);
            return null;
        }
    }

    private String mapSceneType(AnalyzeSceneType sceneType) {
        if (sceneType == null) return "general";
        return switch (sceneType) {
            case SECKILL -> "seckill";
            case SHOP_READ -> "shop-read";
            case LOGIN -> "login";
            default -> "general";
        };
    }

    @lombok.Value
    public static class FileWriteResult {
        String fileName;
        String filePath;
    }
}
