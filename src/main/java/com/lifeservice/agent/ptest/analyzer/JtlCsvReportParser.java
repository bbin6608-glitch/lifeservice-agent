package com.lifeservice.agent.ptest.analyzer;

import com.lifeservice.agent.ptest.analyzer.model.ParsedPerformanceReport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class JtlCsvReportParser {

    public Optional<ParsedPerformanceReport> parse(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            log.error("Report file not found: {}", filePath);
            return Optional.empty();
        }

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            CSVParser parser = format.parse(reader);
            List<CSVRecord> records = parser.getRecords();
            if (records.isEmpty()) {
                return Optional.empty();
            }

            long totalSamples = 0;
            long successCount = 0;
            long errorCount = 0;
            long totalElapsed = 0;
            long maxElapsed = Long.MIN_VALUE;
            long minElapsed = Long.MAX_VALUE;
            List<Long> elapsedList = new ArrayList<>();
            Map<String, Integer> responseCodeCounts = new HashMap<>();
            
            long minTimestamp = Long.MAX_VALUE;
            long maxTimestamp = Long.MIN_VALUE;

            for (CSVRecord record : records) {
                totalSamples++;
                
                // Elapsed time
                long elapsed = getLong(record, "elapsed", "Latency", "latency", "Connect", "connect", "t");
                totalElapsed += elapsed;
                maxElapsed = Math.max(maxElapsed, elapsed);
                minElapsed = Math.min(minElapsed, elapsed);
                elapsedList.add(elapsed);

                // Success
                boolean success = getBoolean(record, "success", "s");
                if (success) successCount++;
                else errorCount++;

                // Response Code
                String code = getString(record, "responseCode", "rc");
                if (code != null) {
                    responseCodeCounts.put(code, responseCodeCounts.getOrDefault(code, 0) + 1);
                }

                // Timestamp for throughput
                long ts = getLong(record, "timeStamp", "ts");
                if (ts > 0) {
                    minTimestamp = Math.min(minTimestamp, ts);
                    maxTimestamp = Math.max(maxTimestamp, ts);
                }
            }

            Collections.sort(elapsedList);
            double p95 = getPercentile(elapsedList, 0.95);
            double p99 = getPercentile(elapsedList, 0.99);
            double avg = (double) totalElapsed / totalSamples;
            double errorRate = (double) errorCount / totalSamples;
            
            double throughput = 0;
            if (maxTimestamp > minTimestamp) {
                double durationSec = (maxTimestamp - minTimestamp) / 1000.0;
                if (durationSec > 0) {
                    throughput = totalSamples / durationSec;
                }
            }

            return Optional.of(ParsedPerformanceReport.builder()
                    .totalSamples(totalSamples)
                    .successCount(successCount)
                    .errorCount(errorCount)
                    .avgElapsed(avg)
                    .maxElapsed(maxElapsed)
                    .minElapsed(minElapsed)
                    .p95Elapsed(p95)
                    .p99Elapsed(p99)
                    .throughputPerSecond(throughput)
                    .errorRate(errorRate)
                    .responseCodeCounts(responseCodeCounts)
                    .build());

        } catch (Exception e) {
            log.error("Failed to parse report file: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private long getLong(CSVRecord record, String... names) {
        for (String name : names) {
            try {
                if (record.isMapped(name)) {
                    return Long.parseLong(record.get(name));
                }
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private String getString(CSVRecord record, String... names) {
        for (String name : names) {
            if (record.isMapped(name)) {
                return record.get(name);
            }
        }
        return null;
    }

    private boolean getBoolean(CSVRecord record, String... names) {
        for (String name : names) {
            if (record.isMapped(name)) {
                String val = record.get(name);
                return "true".equalsIgnoreCase(val) || "1".equals(val);
            }
        }
        return true;
    }

    private double getPercentile(List<Long> sortedList, double percentile) {
        if (sortedList.isEmpty()) return 0;
        int index = (int) Math.ceil(percentile * sortedList.size()) - 1;
        return sortedList.get(Math.max(0, index));
    }
}
