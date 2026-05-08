package com.geek.threaddoctor.loganalysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.prompt.PromptTestFactory;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SidecarLogAnalysisServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void reportsHealthWithLocalCapabilities() {
        SidecarLogAnalysisService service = service(properties(10, 100_000, 100_000));

        SidecarHealth health = service.health();

        assertThat(health.status()).isEqualTo("UP");
        assertThat(health.capabilities()).contains("zip-analysis", "directory-analysis", "local-search");
        assertThat(health.limits()).containsKey("maxFiles");
    }

    @Test
    void analyzesZipAndSupportsLocalSearch() throws Exception {
        Path zipPath = tempDir.resolve("incident.zip");
        writeZip(zipPath, new ZipFixture("app.log", sampleLog()));
        SidecarLogAnalysisService service = service(properties(10, 100_000, 100_000));

        SidecarAnalysisSnapshot snapshot = service.analyzeZip(zipPath.toString());

        assertThat(snapshot.session().getId()).startsWith("LOCAL-");
        assertThat(snapshot.session().getSources()).extracting(LogSource::name).contains("incident.zip");
        assertThat(snapshot.session().getFileSummaries()).extracting(LogFileSummary::sourceFile).contains("incident.zip/app.log");
        assertThat(service.search(snapshot.session().getId(), new LogSearchRequest(
                null, null, List.of("ERROR"), "payment failed", null, null, null, null, null, 10, true, true)).events())
                .hasSize(1);
    }

    @Test
    void analyzesDirectoryWithRelativeSourceNames() throws Exception {
        Path logs = Files.createDirectory(tempDir.resolve("logs"));
        Files.writeString(logs.resolve("app.log"), sampleLog());
        SidecarLogAnalysisService service = service(properties(10, 100_000, 100_000));

        SidecarAnalysisSnapshot snapshot = service.analyzeDirectory(logs.toString());

        assertThat(snapshot.session().getSources()).extracting(LogSource::name).contains("logs");
        assertThat(snapshot.session().getFileSummaries()).extracting(LogFileSummary::sourceFile).contains("app.log");
    }

    @Test
    void rejectsUnsafeZipAndDirectoryLimits() throws Exception {
        Path zipPath = tempDir.resolve("evil.zip");
        writeZip(zipPath, new ZipFixture("../evil.log", sampleLog()));
        SidecarLogAnalysisService service = service(properties(1, 100_000, 100_000));

        assertThatThrownBy(() -> service.analyzeZip(zipPath.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SIDECAR_ZIP_SLIP");

        Path logs = Files.createDirectory(tempDir.resolve("too-many"));
        Files.writeString(logs.resolve("a.log"), sampleLog());
        Files.writeString(logs.resolve("b.log"), sampleLog());
        assertThatThrownBy(() -> service.analyzeDirectory(logs.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SIDECAR_DIRECTORY_FILE_LIMIT");
    }

    private SidecarLogAnalysisService service(LogAnalysisProperties properties) {
        InMemoryLogAnalysisSessionRepository repository = new InMemoryLogAnalysisSessionRepository(new CacheProperties(1000, 7200));
        SensitiveDataMasker masker = new SensitiveDataMasker();
        LogAnalysisService analysisService = new LogAnalysisService(repository, properties, new LogParser(masker), PromptTestFactory.assemblyService());
        return new SidecarLogAnalysisService(repository, properties,
                new SidecarProperties(18765, List.of("http://localhost:5173"), 600_000, 2, 50),
                new LogParser(masker),
                analysisService);
    }

    private LogAnalysisProperties properties(int maxFiles, long maxCompressed, long maxUncompressed) {
        return new LogAnalysisProperties(true, true, List.of(), maxFiles, maxCompressed,
                maxUncompressed, 100.0, 2000, 8000, 5, 100, 100,
                250_000, 3, 20 * 1024 * 1024L, 2000, 20, true);
    }

    private void writeZip(Path path, ZipFixture... fixtures) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            for (ZipFixture fixture : fixtures) {
                zip.putNextEntry(new ZipEntry(fixture.name()));
                zip.write(fixture.content());
                zip.closeEntry();
            }
        }
        Files.write(path, output.toByteArray());
    }

    private String sampleLog() {
        return """
                2026-05-05 10:00:00.000 INFO [main] com.geek.demo.PaymentController - start traceId=abc-123
                2026-05-05 10:00:01.000 ERROR [worker-1] com.geek.demo.PaymentService - payment failed token=abc123 traceId=abc-123
                com.geek.demo.PaymentException: timeout
                    at com.geek.demo.PaymentService.charge(PaymentService.java:42)
                """;
    }

    private record ZipFixture(String name, byte[] content) {
        private ZipFixture(String name, String content) {
            this(name, content.getBytes(StandardCharsets.UTF_8));
        }
    }
}
