/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import com.geek.threaddoctor.common.ResourceNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Sidecar 本地日志分析服务。
 *
 * @author zhiwuli
 * @since 2026-05-08
 */
@Service
@Profile("sidecar")
public class SidecarLogAnalysisService {
    private final InMemoryLogAnalysisSessionRepository repository;
    private final LogAnalysisProperties logProperties;
    private final SidecarProperties sidecarProperties;
    private final LogParser parser;
    private final LogAnalysisService analysisService;

    /**
     * 创建 Sidecar 本地日志分析服务。
     *
     * @param repository 日志会话缓存仓储
     * @param logProperties 日志分析配置
     * @param sidecarProperties Sidecar 运行配置
     * @param parser 日志解析器
     * @param analysisService 日志分析服务
     */
    public SidecarLogAnalysisService(InMemoryLogAnalysisSessionRepository repository,
            LogAnalysisProperties logProperties,
            SidecarProperties sidecarProperties,
            LogParser parser,
            LogAnalysisService analysisService) {
        this.repository = repository;
        this.logProperties = logProperties;
        this.sidecarProperties = sidecarProperties;
        this.parser = parser;
        this.analysisService = analysisService;
    }

    SidecarHealth health() {
        return new SidecarHealth("UP", "0.1.0", sidecarProperties.port(),
                List.of("health", "zip-analysis", "directory-analysis", "local-search", "evidence-pack"),
                Map.of(
                        "maxFiles", logProperties.maxFiles(),
                        "maxCompressedBytes", logProperties.maxCompressedBytes(),
                        "maxUncompressedBytes", logProperties.maxUncompressedBytes(),
                        "maxEntryBytes", logProperties.maxEntryBytes(),
                        "maxZipNestingDepth", logProperties.maxZipNestingDepth(),
                        "maxParseMillis", sidecarProperties.maxParseMillis(),
                        "maxWorkers", sidecarProperties.maxWorkers(),
                        "maxResultEvents", sidecarProperties.maxResultEvents()));
    }

    SidecarAnalysisSnapshot analyzeZip(String rawPath) {
        Path path = existingFile(rawPath);
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new IllegalArgumentException("SIDECAR_ZIP_REQUIRED: Sidecar ZIP analysis requires a .zip file.");
        }
        long compressedBytes = size(path);
        if (compressedBytes > logProperties.maxCompressedBytes()) {
            throw new IllegalArgumentException("SIDECAR_ZIP_TOO_LARGE: Local ZIP exceeds configured compressed size limit.");
        }
        LogAnalysisSession session = new LogAnalysisSession("LOCAL-" + UUID.randomUUID(), LocalDateTime.now());
        List<LogEvent> parsedEvents = new ArrayList<>();
        ZipState state = new ZipState(compressedBytes);
        long started = System.currentTimeMillis();
        try (InputStream input = Files.newInputStream(path)) {
            readZip(session, parsedEvents, SidecarPathUtil.displayName(path), input, state, 0, started);
        } catch (IOException ex) {
            throw new IllegalArgumentException("SIDECAR_ZIP_READ_FAILED: " + ex.getMessage(), ex);
        }
        session.addSource(new LogSource(UUID.randomUUID().toString(), LogSourceType.ZIP, SidecarPathUtil.displayName(path), compressedBytes));
        session.replaceEvents(parsedEvents);
        repository.save(session);
        return snapshot(session.getId());
    }

    SidecarAnalysisSnapshot analyzeDirectory(String rawPath) {
        Path root = existingDirectory(rawPath);
        LogAnalysisSession session = new LogAnalysisSession("LOCAL-" + UUID.randomUUID(), LocalDateTime.now());
        List<LogEvent> parsedEvents = new ArrayList<>();
        long totalBytes = 0;
        int fileCount = 0;
        long started = System.currentTimeMillis();
        try (var stream = Files.walk(root)) {
            for (Path file : stream.filter(Files::isRegularFile).toList()) {
                enforceParseTime(started);
                String sourceFile = SidecarPathUtil.safeRelative(root, file);
                if (!supportedSource(sourceFile)) {
                    continue;
                }
                fileCount++;
                if (fileCount > logProperties.maxFiles()) {
                    throw new IllegalArgumentException("SIDECAR_DIRECTORY_FILE_LIMIT: Directory file count exceeds configured limit.");
                }
                long fileSize = Files.size(file);
                if (fileSize > logProperties.maxEntryBytes()) {
                    throw new IllegalArgumentException("SIDECAR_DIRECTORY_ENTRY_SIZE_LIMIT: Directory file exceeds configured per-entry size limit.");
                }
                totalBytes += fileSize;
                if (totalBytes > logProperties.maxUncompressedBytes()) {
                    throw new IllegalArgumentException("SIDECAR_DIRECTORY_SIZE_LIMIT: Directory content exceeds configured limit.");
                }
                ParsedLogFile parsed = parser.parse(sourceFile, Files.readString(file), logProperties);
                session.addFileSummary(parsed.summary());
                parsedEvents.addAll(parsed.events());
                enforceEventLimit(parsedEvents.size());
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("SIDECAR_DIRECTORY_READ_FAILED: " + ex.getMessage(), ex);
        }
        session.addSource(new LogSource(UUID.randomUUID().toString(), LogSourceType.DIRECTORY, SidecarPathUtil.displayName(root), totalBytes));
        session.replaceEvents(parsedEvents);
        repository.save(session);
        return snapshot(session.getId());
    }

    LogSearchResult search(String sessionId, LogSearchRequest request) {
        return analysisService.search(sessionId, request);
    }

    SidecarAnalysisSnapshot snapshot(String sessionId) {
        LogAnalysisSession session = repository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sidecar log analysis session not found: " + sessionId));
        LogSearchResult selected = analysisService.search(sessionId, new LogSearchRequest(
                null, null, List.of(), null, null, null, null, null, null,
                sidecarProperties.maxResultEvents(), true, true, false));
        return new SidecarAnalysisSnapshot(
                session,
                analysisService.clusters(sessionId),
                analysisService.timeline(sessionId),
                analysisService.evidencePack(sessionId),
                analysisService.evidencePackMarkdown(sessionId),
                selected,
                Map.of("mode", "sidecar", "rawLogsSubmitted", "false"));
    }

    private void readZip(LogAnalysisSession session, List<LogEvent> parsedEvents, String archiveName,
            InputStream input, ZipState state, int depth, long started) throws IOException {
        if (depth > logProperties.maxZipNestingDepth()) {
            throw new IllegalArgumentException("SIDECAR_ZIP_NESTING_LIMIT: ZIP nesting depth exceeds configured limit.");
        }
        try (ZipInputStream zip = new ZipInputStream(input, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                enforceParseTime(started);
                if (entry.isDirectory()) {
                    continue;
                }
                state.fileCount++;
                if (state.fileCount > logProperties.maxFiles()) {
                    throw new IllegalArgumentException("SIDECAR_ZIP_FILE_LIMIT: ZIP file count exceeds configured limit.");
                }
                String entryName = safeZipEntryName(entry.getName());
                String sourceFile = safeZipEntryName(archiveName + "/" + entryName);
                if (!supportedSource(sourceFile)) {
                    continue;
                }
                byte[] bytes = readBounded(zip, sourceFile);
                state.totalUncompressed += bytes.length;
                if (state.totalUncompressed > logProperties.maxUncompressedBytes()) {
                    throw new IllegalArgumentException("SIDECAR_ZIP_UNCOMPRESSED_LIMIT: ZIP uncompressed content exceeds configured limit.");
                }
                if (state.rootCompressedSize > 0
                        && (double) state.totalUncompressed / state.rootCompressedSize > logProperties.maxCompressionRatio()) {
                    throw new IllegalArgumentException("SIDECAR_ZIP_RATIO_LIMIT: ZIP decompression ratio exceeds configured limit.");
                }
                if (isZip(sourceFile, bytes)) {
                    readZip(session, parsedEvents, sourceFile, new java.io.ByteArrayInputStream(bytes), state, depth + 1, started);
                    continue;
                }
                ParsedLogFile parsed = parser.parse(sourceFile, new String(bytes, StandardCharsets.UTF_8), logProperties);
                session.addFileSummary(parsed.summary());
                parsedEvents.addAll(parsed.events());
                enforceEventLimit(parsedEvents.size());
            }
        }
    }

    private byte[] readBounded(InputStream input, String sourceFile) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
            if (output.size() > logProperties.maxEntryBytes()) {
                throw new IllegalArgumentException("SIDECAR_ZIP_ENTRY_SIZE_LIMIT: ZIP entry exceeds configured per-entry size limit: " + sourceFile);
            }
        }
        return output.toByteArray();
    }

    private Path existingFile(String rawPath) {
        Path path = safeLocalPath(rawPath);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("SIDECAR_FILE_NOT_FOUND: Local file does not exist.");
        }
        return path;
    }

    private Path existingDirectory(String rawPath) {
        Path path = safeLocalPath(rawPath);
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("SIDECAR_DIRECTORY_NOT_FOUND: Local directory does not exist.");
        }
        return path;
    }

    private Path safeLocalPath(String rawPath) {
        if (rawPath == null || rawPath.isBlank() || rawPath.length() > 2048) {
            throw new IllegalArgumentException("SIDECAR_PATH_INVALID: Local source path is invalid.");
        }
        return Path.of(rawPath).toAbsolutePath().normalize();
    }

    private long size(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ex) {
            throw new IllegalArgumentException("SIDECAR_FILE_SIZE_FAILED: " + ex.getMessage(), ex);
        }
    }

    private String safeZipEntryName(String entryName) {
        Path normalized = Path.of(entryName).normalize();
        if (normalized.isAbsolute() || normalized.startsWith("..") || entryName.contains("..\\")) {
            throw new IllegalArgumentException("SIDECAR_ZIP_SLIP: ZIP entry path traversal is not allowed.");
        }
        return normalized.toString().replace('\\', '/');
    }

    private boolean supportedSource(String sourceFile) {
        String normalized = sourceFile.toLowerCase(Locale.ROOT);
        return normalized.endsWith(".zip")
                || normalized.endsWith(".log")
                || normalized.endsWith(".txt")
                || normalized.endsWith(".out");
    }

    private boolean isZip(String sourceFile, byte[] bytes) {
        return sourceFile.toLowerCase(Locale.ROOT).endsWith(".zip")
                && bytes.length >= 4
                && bytes[0] == 'P'
                && bytes[1] == 'K';
    }

    private void enforceEventLimit(int eventCount) {
        if (eventCount > logProperties.maxEventsPerSession()) {
            throw new IllegalArgumentException("SIDECAR_LOG_EVENT_LIMIT: Parsed log event count exceeds configured session limit.");
        }
    }

    private void enforceParseTime(long started) {
        if (System.currentTimeMillis() - started > sidecarProperties.maxParseMillis()) {
            throw new IllegalArgumentException("SIDECAR_PARSE_TIMEOUT: Local parsing exceeded configured time limit.");
        }
    }

    private static final class ZipState {
        private final long rootCompressedSize;
        private long totalUncompressed;
        private int fileCount;

        private ZipState(long rootCompressedSize) {
            this.rootCompressedSize = rootCompressedSize;
        }
    }
}
