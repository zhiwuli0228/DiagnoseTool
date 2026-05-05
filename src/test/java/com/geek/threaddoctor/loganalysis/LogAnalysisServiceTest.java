package com.geek.threaddoctor.loganalysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.prompt.PromptTestFactory;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LogAnalysisServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void uploadZipParsesLogsStackTraceAndTraceId() throws Exception {
        LogAnalysisService service = service(properties(true, true, List.of(tempDir.toString()), 10, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();

        service.uploadZip(session.getId(), zipFile("app.log", sampleLog()));

        LogAnalysisSession stored = service.getSession(session.getId());
        assertThat(stored.getSources()).hasSize(1);
        assertThat(stored.getFileSummaries()).hasSize(1);
        assertThat(stored.getEventCount()).isEqualTo(3);

        LogSearchResult result = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of("ERROR"), "payment failed", "abc-123", null, null, null, null, 10, true, true));
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().getFirst().exceptionType()).isEqualTo("com.geek.demo.PaymentException");
        assertThat(result.events().getFirst().stackTrace()).contains("PaymentService.charge");
    }

    @Test
    void uploadZipRecursivelyParsesNestedLogArchives() throws Exception {
        LogAnalysisService service = service(properties(true, false, List.of(), 10, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();

        service.uploadZip(session.getId(), zipFile(new ZipFixture("collect_dadasdasd1322_dasd.log.zip", nestedZipBytes("nested/app.log", sampleLog()))));

        LogAnalysisSession stored = service.getSession(session.getId());
        assertThat(stored.getFileSummaries())
                .extracting(LogFileSummary::sourceFile)
                .anyMatch(source -> source.contains("collect_dadasdasd1322_dasd.log.zip"));
        assertThat(service.search(session.getId(), new LogSearchRequest(
                null, null, List.of("ERROR"), "payment failed", null, null, null, null, null, 10, true, true)).events())
                .hasSize(1);
    }

    @Test
    void rejectsZipSlipAndStoresStructuredError() throws Exception {
        LogAnalysisService service = service(properties(true, false, List.of(), 10, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();

        assertThatThrownBy(() -> service.uploadZip(session.getId(), zipFile("../evil.log", sampleLog())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ZIP_SLIP");
        assertThat(service.getSession(session.getId()).getErrors())
                .extracting(LogAnalysisError::code)
                .contains("ZIP_SLIP");
    }

    @Test
    void rejectsZipFileCountLimit() throws Exception {
        LogAnalysisService service = service(properties(true, false, List.of(), 1, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();

        assertThatThrownBy(() -> service.uploadZip(session.getId(), zipFile(
                new ZipFixture("a.log", sampleLog()),
                new ZipFixture("b.log", sampleLog()))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ZIP_FILE_LIMIT");
    }

    @Test
    void rejectsDirectoryOutsideAllowlistAndScansAllowedDirectory() throws Exception {
        Path allowed = Files.createDirectory(tempDir.resolve("allowed"));
        Path disallowed = Files.createDirectory(tempDir.resolve("disallowed"));
        Files.writeString(allowed.resolve("app.log"), sampleLog());
        LogAnalysisService service = service(properties(true, true, List.of(allowed.toString()), 10, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();

        assertThatThrownBy(() -> service.scanDirectory(session.getId(), disallowed.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DIRECTORY_NOT_ALLOWED");

        service.scanDirectory(session.getId(), allowed.toString());
        assertThat(service.getSession(session.getId()).getSources())
                .extracting(LogSource::type)
                .contains(LogSourceType.DIRECTORY);
    }

    @Test
    void uploadsBrowserDirectoryFilesAndKeepsRelativePaths() throws Exception {
        LogAnalysisService service = service(properties(true, true, List.of(), 10, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();
        MockMultipartFile file = new MockMultipartFile("files", "incident-001/server.log", "text/plain",
                sampleLog().getBytes(StandardCharsets.UTF_8));

        service.uploadDirectoryFiles(session.getId(), new MockMultipartFile[] { file });

        LogAnalysisSession stored = service.getSession(session.getId());
        assertThat(stored.getSources())
                .extracting(LogSource::type)
                .contains(LogSourceType.DIRECTORY);
        assertThat(stored.getFileSummaries())
                .extracting(LogFileSummary::sourceFile)
                .contains("incident-001/server.log");
    }

    @Test
    void preservesMalformedLinesAndSearchCanExcludeStackTrace() throws Exception {
        LogAnalysisService service = service(properties(true, false, List.of(), 10, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();
        service.uploadZip(session.getId(), zipFile("app.log", sampleLog()));

        LogSearchResult unparsed = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of("UNPARSED"), "broken", null, null, null, null, null, 10, false, true));
        assertThat(unparsed.events()).hasSize(1);
        assertThat(unparsed.events().getFirst().level()).isEqualTo("UNPARSED");

        LogSearchResult withoutStack = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of("ERROR"), null, null, null, null, null, null, 10, false, true));
        assertThat(withoutStack.events().getFirst().stackTrace()).isNull();
    }

    @Test
    void searchesTraceSourceRawTextAndSplitKeywordsWithoutDefaultLevelFilter() throws Exception {
        LogAnalysisService service = service(properties(true, false, List.of(), 10, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();
        service.uploadZip(session.getId(), zipFile("payment/app-info.log", sampleLog()));

        LogSearchResult traceId = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of(), "abc-123", null, null, null, null, null, 10, true, true));
        assertThat(traceId.events())
                .extracting(LogEvent::level)
                .contains("INFO");

        LogSearchResult sourceFile = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of(), "app-info.log", null, null, null, null, null, 10, true, true));
        assertThat(sourceFile.events()).isNotEmpty();

        LogSearchResult splitKeyword = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of(), "not-present\nbroken", null, null, null, null, null, 10, true));
        assertThat(splitKeyword.events())
                .extracting(LogEvent::level)
                .contains("UNPARSED");
    }

    @Test
    void searchesMultipleKeywordsLevelsCaseSensitivityAndTimeRange() throws Exception {
        LogAnalysisService service = service(properties(true, false, List.of(), 10, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();
        service.uploadZip(session.getId(), zipFile("app.log", sampleLog()));

        LogSearchResult multiKeyword = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of(), "Payment\ntimeout", null, null, null, null, null, 10, true, true));
        assertThat(multiKeyword.events())
                .extracting(LogEvent::level)
                .contains("ERROR");

        service.uploadZip(session.getId(), zipFile("punctuation.log",
                "2026-05-05 10:00:03.000 ERROR [worker-2] com.geek.demo.PaymentService - marker=a,b;c retry payment failed"));
        LogSearchResult punctuationPhrase = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of(), "marker=a,b;c retry", null, null, null, null, null, 10, true, true));
        assertThat(punctuationPhrase.events())
                .extracting(LogEvent::message)
                .anyMatch(message -> message.contains("marker=a,b;c retry"));

        LogSearchResult caseSensitiveMiss = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of(), "PAYMENT", null, null, null, null, null, 10, true, false));
        assertThat(caseSensitiveMiss.events()).isEmpty();

        LogSearchResult timeBound = service.search(session.getId(), new LogSearchRequest(
                LocalDateTime.parse("2026-05-05T10:00:00"),
                LocalDateTime.parse("2026-05-05T10:00:00"),
                List.of("INFO"), "start", null, null, null, null, null, 10, true, true));
        assertThat(timeBound.events())
                .extracting(LogEvent::level)
                .containsExactly("INFO");
    }

    @Test
    void deduplicatesEquivalentSearchResultsWhenRequested() throws Exception {
        LogAnalysisService service = service(properties(true, false, List.of(), 10, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();
        service.uploadZip(session.getId(), zipFile("app.log", """
                2026-05-05 10:00:00.000 INFO [main] com.geek.demo.PaymentService - retry payment failed
                2026-05-05 10:00:01.000 INFO [main] com.geek.demo.PaymentService - retry payment failed
                2026-05-05 10:00:02.000 INFO [main] com.geek.demo.PaymentService - retry payment failed
                """));

        LogSearchResult result = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of("INFO"), "retry payment failed", null, null, null, null, null,
                10, true, true, true));

        assertThat(result.totalMatched()).isEqualTo(3);
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().getFirst().duplicateCount()).isEqualTo(3);
    }

    @Test
    void createsClustersTimelineEvidencePackAndGeneratedArtifacts() throws Exception {
        LogAnalysisService service = service(properties(true, false, List.of(), 10, 100_000, 100_000, 2000, 8000));
        LogAnalysisSession session = service.createSession();
        service.uploadZip(session.getId(), zipFile("app.log", sampleLog()));

        List<LogCluster> clusters = service.clusters(session.getId());
        assertThat(clusters).isNotEmpty();
        assertThat(clusters.getFirst().suspectedClasses()).contains("com.geek.demo.PaymentService");

        IncidentTimeline timeline = service.timeline(session.getId());
        assertThat(timeline.events()).isNotEmpty();
        assertThat(timeline.events().getFirst().traceId()).isEqualTo("abc-123");

        EvidencePack pack = service.evidencePack(session.getId());
        assertThat(pack.evidenceItems()).isNotEmpty();
        assertThat(pack.suspectedCodeAreas()).isNotEmpty();
        assertThat(service.evidencePackMarkdown(session.getId())).contains("Evidence Pack");
        assertThat(service.codexTask(session.getId()).markdown()).contains("JUnit 5 with Mockito");
        assertThat(service.openSpecChangeDraft(session.getId()).markdown()).contains("OpenSpec Change Draft");
    }

    @Test
    void masksSensitiveDataAndBoundsOutput() throws Exception {
        LogAnalysisService service = service(properties(true, false, List.of(), 10, 100_000, 100_000, 80, 120));
        LogAnalysisSession session = service.createSession();
        String log = "2026-05-05 10:00:00.000 ERROR [main] com.geek.demo.AuthService - failed token=abc123 password=secret email user@example.com from 10.1.2.3 "
                + "x".repeat(200);

        service.uploadZip(session.getId(), zipFile("secure.log", log));

        LogEvent event = service.search(session.getId(), new LogSearchRequest(
                null, null, List.of("ERROR"), null, null, null, null, null, null, 10, true, true)).events().getFirst();
        assertThat(event.message()).contains("[SECRET]").contains("[EMAIL]").contains("[IP]");
        assertThat(event.message()).contains("[truncated]");
    }

    private LogAnalysisService service(LogAnalysisProperties properties) {
        InMemoryLogAnalysisSessionRepository repository = new InMemoryLogAnalysisSessionRepository(new CacheProperties(1000, 7200));
        SensitiveDataMasker masker = new SensitiveDataMasker();
        return new LogAnalysisService(repository, properties, new LogParser(masker), PromptTestFactory.assemblyService());
    }

    private LogAnalysisProperties properties(boolean zipEnabled, boolean directoryScanEnabled, List<String> roots,
            int maxFiles, long maxCompressed, long maxUncompressed, int rawLimit, int stackLimit) {
        return new LogAnalysisProperties(zipEnabled, directoryScanEnabled, roots, maxFiles, maxCompressed,
                maxUncompressed, 100.0, rawLimit, stackLimit, 5, 100, 100, true);
    }

    private MockMultipartFile zipFile(String name, String content) throws Exception {
        return zipFile(new ZipFixture(name, content));
    }

    private MockMultipartFile zipFile(ZipFixture... fixtures) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            for (ZipFixture fixture : fixtures) {
                zip.putNextEntry(new ZipEntry(fixture.name()));
                zip.write(fixture.content());
                zip.closeEntry();
            }
        }
        return new MockMultipartFile("file", "logs.zip", "application/zip", output.toByteArray());
    }

    private String sampleLog() {
        return """
                2026-05-05 10:00:00.000 INFO [main] com.geek.demo.PaymentController - start traceId=abc-123
                2026-05-05 10:00:01.000 ERROR [worker-1] com.geek.demo.PaymentService - payment failed traceId=abc-123
                com.geek.demo.PaymentException: timeout
                    at com.geek.demo.PaymentService.charge(PaymentService.java:42)
                    at org.springframework.web.method.HandlerMethod.invoke(HandlerMethod.java:1)
                Caused by: java.net.SocketTimeoutException: Read timed out
                this is a broken log line
                """;
    }

    private byte[] nestedZipBytes(String name, String content) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            zip.putNextEntry(new ZipEntry(name));
            zip.write(content.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return output.toByteArray();
    }

    private record ZipFixture(String name, byte[] content) {
        private ZipFixture(String name, String content) {
            this(name, content.getBytes(StandardCharsets.UTF_8));
        }
    }
}

