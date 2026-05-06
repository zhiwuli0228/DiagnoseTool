package com.geek.threaddoctor.loganalysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.prompt.PromptTestFactory;
import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class LogAnalysisPerformanceIT {
    private static final int LINE_COUNT = 120_000;
    private static final int ERROR_INTERVAL = 40;
    private static final int TARGET_INTERVAL = 6_000;

    @Test
    void measureLargeZipIngestionAndSearch() throws Exception {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        LogAnalysisService service = service();
        LogAnalysisSession session = service.createSession();

        String logContent = largeLogContent();
        long uncompressedBytes = logContent.getBytes(StandardCharsets.UTF_8).length;
        MockMultipartFile zipFile = zipFile("large-app.log", logContent);
        long compressedBytes = zipFile.getSize();

        forceGc();
        long memoryBefore = usedHeap(memory);
        Measurement upload = measure(() -> service.uploadZip(session.getId(), zipFile));
        long memoryAfterUpload = usedHeap(memory);

        LogAnalysisSession stored = service.getSession(session.getId());
        assertThat(stored.getEventCount()).isEqualTo(LINE_COUNT);

        Measurement errorSearch = measure(() -> service.search(session.getId(), new LogSearchRequest(
                null, null, List.of("ERROR"), null, null, null, null, null, null,
                200, false, true)));
        LogSearchResult errorResult = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of("ERROR"), null, null, null, null, null, null,
                200, false, true));

        Measurement rareKeywordSearch = measure(() -> service.search(session.getId(), new LogSearchRequest(
                null, null, List.of(), "target-marker", null, null, null, null, null,
                200, false, true)));
        LogSearchResult rareKeywordResult = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of(), "target-marker", null, null, null, null, null,
                200, false, true));

        Measurement missKeywordSearch = measure(() -> service.search(session.getId(), new LogSearchRequest(
                null, null, List.of(), "keyword-not-present", null, null, null, null, null,
                200, false, true)));
        LogSearchResult missKeywordResult = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of(), "keyword-not-present", null, null, null, null, null,
                200, false, true));

        Path report = Path.of("target", "log-analysis-performance-report.md");
        Files.createDirectories(report.getParent());
        Files.writeString(report, reportMarkdown(
                uncompressedBytes,
                compressedBytes,
                stored.getEventCount(),
                upload,
                memoryAfterUpload - memoryBefore,
                errorSearch,
                errorResult.totalMatched(),
                rareKeywordSearch,
                rareKeywordResult.totalMatched(),
                missKeywordSearch,
                missKeywordResult.totalMatched()), StandardCharsets.UTF_8);
    }

    private LogAnalysisService service() {
        LogAnalysisProperties properties = new LogAnalysisProperties(
                true,
                false,
                List.of(),
                10,
                80L * 1024 * 1024,
                180L * 1024 * 1024,
                10_000.0,
                2_000,
                8_000,
                5,
                200,
                200,
                true);
        return new LogAnalysisService(
                new InMemoryLogAnalysisSessionRepository(new CacheProperties(10, 7_200)),
                properties,
                new LogParser(new SensitiveDataMasker()),
                PromptTestFactory.assemblyService());
    }

    private String largeLogContent() {
        StringBuilder builder = new StringBuilder(LINE_COUNT * 170);
        for (int i = 0; i < LINE_COUNT; i++) {
            String level = i % ERROR_INTERVAL == 0 ? "ERROR" : i % 11 == 0 ? "WARN" : "INFO";
            String marker = i % TARGET_INTERVAL == 0 ? " target-marker" : "";
            builder.append("2026-05-06 10:")
                    .append(twoDigits((i / 60) % 60))
                    .append(":")
                    .append(twoDigits(i % 60))
                    .append(".")
                    .append(String.format(Locale.ROOT, "%03d", i % 1000))
                    .append(" ")
                    .append(level)
                    .append(" [worker-")
                    .append(i % 64)
                    .append("] com.geek.demo.LargeLogService - process orderId=")
                    .append(i)
                    .append(" traceId=trace-")
                    .append(i % 20_000)
                    .append(marker)
                    .append(" status=")
                    .append(level.equals("ERROR") ? "failed timeout" : "ok")
                    .append(" payload=abcdefghijklmnopqrstuvwxyz0123456789\n");
        }
        return builder.toString();
    }

    private String twoDigits(int value) {
        return value < 10 ? "0" + value : Integer.toString(value);
    }

    private MockMultipartFile zipFile(String name, String content) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            zip.putNextEntry(new ZipEntry(name));
            zip.write(content.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return new MockMultipartFile("file", "large-logs.zip", "application/zip", output.toByteArray());
    }

    private Measurement measure(CheckedRunnable runnable) throws Exception {
        Instant start = Instant.now();
        runnable.run();
        return new Measurement(Duration.between(start, Instant.now()).toMillis());
    }

    private void forceGc() throws InterruptedException {
        System.gc();
        Thread.sleep(250);
    }

    private long usedHeap(MemoryMXBean memory) {
        MemoryUsage usage = memory.getHeapMemoryUsage();
        return usage.getUsed();
    }

    private String reportMarkdown(long uncompressedBytes,
            long compressedBytes,
            int events,
            Measurement upload,
            long heapDeltaBytes,
            Measurement errorSearch,
            int errorMatches,
            Measurement rareKeywordSearch,
            int rareKeywordMatches,
            Measurement missKeywordSearch,
            int missKeywordMatches) {
        return """
                # Log Analysis Performance Test Report

                ## Scope

                This report measures the current in-process ZIP ingestion, parsing, and bounded search implementation.
                The test uses generated Java application logs and calls `LogAnalysisService.uploadZip` and `LogAnalysisService.search` directly.

                ## Dataset

                - Log files in ZIP: 1
                - Log lines / parsed events: %d
                - Uncompressed size: %s
                - ZIP size: %s
                - ERROR interval: every %d lines
                - Target keyword interval: every %d lines

                ## Results

                | Operation | Matched rows | Time |
                | --- | ---: | ---: |
                | ZIP upload + unzip + parse | %d | %d ms |
                | Search by level `ERROR` | %d | %d ms |
                | Search by keyword `target-marker` | %d | %d ms |
                | Search keyword miss | %d | %d ms |

                ## Memory

                - Heap delta after upload and parse: %s

                ## Findings

                - ZIP ingestion is not streaming end-to-end. The current code reads the uploaded multipart file into a byte array, reads each ZIP entry into a byte array, converts each entry to a full UTF-8 string, and then splits the full content into lines.
                - Parsed events are stored in the session cache as a full `List<LogEvent>`. Search is therefore bounded in returned rows but still scans all events in the session.
                - Search complexity is linear with the number of parsed events. Filters and limits reduce returned payload size, but they do not avoid scanning the full cached event list.
                - This is acceptable for moderate bounded uploads, but large production log bundles will be constrained by heap size and per-search scan time.

                ## Recommendations

                - Change ZIP and directory ingestion to stream entries line by line instead of materializing full files as byte arrays and strings.
                - Add a bounded per-session event cap or spill parsed events to temporary files when the event count crosses a configured threshold.
                - Add lightweight in-memory indexes for common filters such as level, traceId, sourceFile, and normalized keyword tokens if repeated searches over the same session are expected.
                - Keep the current `max-uncompressed-bytes`, `max-files`, and `max-search-limit` safeguards enabled in production.
                """.formatted(
                events,
                humanBytes(uncompressedBytes),
                humanBytes(compressedBytes),
                ERROR_INTERVAL,
                TARGET_INTERVAL,
                events,
                upload.millis(),
                errorMatches,
                errorSearch.millis(),
                rareKeywordMatches,
                rareKeywordSearch.millis(),
                missKeywordMatches,
                missKeywordSearch.millis(),
                humanBytes(heapDeltaBytes));
    }

    private String humanBytes(long bytes) {
        double value = bytes;
        String[] units = {"B", "KB", "MB", "GB"};
        int unit = 0;
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        return "%.2f %s".formatted(value, units[unit]);
    }

    private record Measurement(long millis) {
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
