/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.prompt.PromptAssemblyService;
import com.geek.threaddoctor.security.ArtifactSanitizer;
import com.geek.threaddoctor.security.SecurityLimitsProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Service
public class LogAnalysisService {
    private static final Pattern STACK_FRAME = Pattern.compile("\\bat\\s+([A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*)\\.([A-Za-z_$][\\w$]*)\\(");
    private static final List<String> EXCLUDED_CLASS_PREFIXES = List.of(
            "java.", "javax.", "jakarta.", "sun.", "jdk.", "org.springframework.", "redis.clients.",
            "org.apache.", "com.zaxxer.", "org.slf4j.");
    private static final List<String> HIGH_RISK_KEYWORDS = List.of(
            "timeout", "failed", "rejected", "oom", "deadlock", "pool exhausted", "connection refused");

    private final InMemoryLogAnalysisSessionRepository repository;
    private final LogAnalysisProperties properties;
    private final LogParser parser;
    private final PromptAssemblyService promptAssemblyService;
    private final ArtifactSanitizer artifactSanitizer;

    /**
     * 执行业务操作。
     *
     * @param repository 仓储依赖
     * @param properties 配置属性
     * @param parser 业务参数
     * @param promptAssemblyService 业务服务依赖
     * @param artifactSanitizer 业务参数
     */
    @Autowired
    public LogAnalysisService(InMemoryLogAnalysisSessionRepository repository,
            LogAnalysisProperties properties,
            LogParser parser,
            PromptAssemblyService promptAssemblyService,
            ArtifactSanitizer artifactSanitizer) {
        this.repository = repository;
        this.properties = properties;
        this.parser = parser;
        this.promptAssemblyService = promptAssemblyService;
        this.artifactSanitizer = artifactSanitizer;
    }

    /**
     * 执行业务操作。
     *
     * @param repository 仓储依赖
     * @param properties 配置属性
     * @param parser 业务参数
     * @param promptAssemblyService 业务服务依赖
     */
    public LogAnalysisService(InMemoryLogAnalysisSessionRepository repository,
            LogAnalysisProperties properties,
            LogParser parser,
            PromptAssemblyService promptAssemblyService) {
        this(repository, properties, parser, promptAssemblyService,
                new ArtifactSanitizer(new SensitiveDataMasker(), new SecurityLimitsProperties(
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
    }

    /**
     * 创建日志分析会话。
     *
     * @return 日志分析会话
     */
    public LogAnalysisSession createSession() {
        LogAnalysisSession session = new LogAnalysisSession("LOG-" + UUID.randomUUID(), LocalDateTime.now());
        return repository.save(session);
    }

    /**
     * 获取日志分析会话。
     *
     * @param sessionId 会话标识
     * @return 日志分析会话
     */
    public LogAnalysisSession getSession(String sessionId) {
        return repository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Log analysis session not found: " + sessionId));
    }

    /**
     * 上传并分析压缩日志。
     *
     * @param sessionId 会话标识
     * @param file 上传文件
     * @return 更新后的日志分析会话
     */
    public LogAnalysisSession uploadZip(String sessionId, MultipartFile file) {
        if (!properties.zipEnabled()) {
            throw structuredFailure(sessionId, "ZIP_DISABLED", "ZIP log ingestion is disabled.", null);
        }
        if (file == null || file.isEmpty()) {
            throw structuredFailure(sessionId, "EMPTY_SOURCE", "Uploaded ZIP is empty.", null);
        }
        if (file.getSize() > properties.maxCompressedBytes()) {
            throw structuredFailure(sessionId, "ZIP_TOO_LARGE", "Uploaded ZIP exceeds configured compressed size limit.", file.getOriginalFilename());
        }
        validateZipUpload(sessionId, file);
        LogAnalysisSession session = getSession(sessionId);
        List<LogEvent> parsedEvents = new ArrayList<>(session.events());
        ZipReadState state = new ZipReadState();
        try {
            readZipEntries(sessionId, file.getOriginalFilename(), file.getBytes(), file.getSize(), session, parsedEvents, state, 0);
        } catch (IOException ex) {
            throw structuredFailure(sessionId, "ZIP_READ_FAILED", ex.getMessage(), file.getOriginalFilename());
        }
        session.addSource(new LogSource(UUID.randomUUID().toString(), LogSourceType.ZIP, file.getOriginalFilename(), file.getSize()));
        session.replaceEvents(parsedEvents);
        return repository.save(session);
    }

    /**
     * 上传并分析目录日志文件。
     *
     * @param sessionId 会话标识
     * @param files 上传文件集合
     * @return 更新后的日志分析会话
     */
    public LogAnalysisSession uploadDirectoryFiles(String sessionId, MultipartFile[] files) {
        if (!properties.directoryScanEnabled()) {
            throw structuredFailure(sessionId, "DIRECTORY_UPLOAD_DISABLED", "Directory upload is disabled.", null);
        }
        if (files == null || files.length == 0) {
            throw structuredFailure(sessionId, "EMPTY_SOURCE", "Uploaded directory is empty.", null);
        }
        if (files.length > properties.maxFiles()) {
            throw structuredFailure(sessionId, "DIRECTORY_FILE_LIMIT", "Directory file count exceeds configured limit.", null);
        }
        LogAnalysisSession session = getSession(sessionId);
        List<LogEvent> parsedEvents = new ArrayList<>(session.events());
        long totalBytes = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String sourceFile = safeZipEntryName(sessionId, file.getOriginalFilename() == null ? file.getName() : file.getOriginalFilename());
            validateSupportedSourceFile(sessionId, sourceFile);
            try {
                byte[] bytes = file.getBytes();
                if (bytes.length > properties.maxEntryBytes()) {
                    throw structuredFailure(sessionId, "DIRECTORY_ENTRY_SIZE_LIMIT", "Directory file exceeds configured per-entry size limit.", sourceFile);
                }
                totalBytes += bytes.length;
                if (totalBytes > properties.maxUncompressedBytes()) {
                    throw structuredFailure(sessionId, "DIRECTORY_SIZE_LIMIT", "Directory content exceeds configured limit.", sourceFile);
                }
                if (isNestedZip(sourceFile, bytes)) {
                    readZipEntries(sessionId, sourceFile, bytes, bytes.length, session, parsedEvents, new ZipReadState(), 0);
                    continue;
                }
                ParsedLogFile parsed = parser.parse(sourceFile, new String(bytes, StandardCharsets.UTF_8), properties);
                session.addFileSummary(parsed.summary());
                parsedEvents.addAll(parsed.events());
                enforceEventLimit(sessionId, parsedEvents.size(), sourceFile);
            } catch (IOException ex) {
                throw structuredFailure(sessionId, "DIRECTORY_READ_FAILED", ex.getMessage(), sourceFile);
            }
        }
        session.addSource(new LogSource(UUID.randomUUID().toString(), LogSourceType.DIRECTORY, "browser-upload", totalBytes));
        session.replaceEvents(parsedEvents);
        return repository.save(session);
    }

    private void readZipEntries(String sessionId, String archiveName, byte[] archiveBytes, long rootCompressedSize,
            LogAnalysisSession session, List<LogEvent> parsedEvents, ZipReadState state, int depth) throws IOException {
        if (depth > properties.maxZipNestingDepth()) {
            throw structuredFailure(sessionId, "ZIP_NESTING_LIMIT", "ZIP nesting depth exceeds configured limit.", archiveName);
        }
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archiveBytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                state.fileCount++;
                if (state.fileCount > properties.maxFiles()) {
                    throw structuredFailure(sessionId, "ZIP_FILE_LIMIT", "ZIP file count exceeds configured limit.", archiveName);
                }
                String entryName = safeZipEntryName(sessionId, entry.getName());
                String sourceFile = safeZipEntryName(sessionId, archiveName + "/" + entryName);
                validateSupportedSourceFile(sessionId, sourceFile);
                byte[] bytes = zip.readAllBytes();
                if (bytes.length > properties.maxEntryBytes()) {
                    throw structuredFailure(sessionId, "ZIP_ENTRY_SIZE_LIMIT", "ZIP entry exceeds configured per-entry size limit.", sourceFile);
                }
                state.totalUncompressed += bytes.length;
                if (state.totalUncompressed > properties.maxUncompressedBytes()) {
                    throw structuredFailure(sessionId, "ZIP_UNCOMPRESSED_LIMIT", "ZIP uncompressed content exceeds configured limit.", sourceFile);
                }
                if (rootCompressedSize > 0 && (double) state.totalUncompressed / rootCompressedSize > properties.maxCompressionRatio()) {
                    throw structuredFailure(sessionId, "ZIP_RATIO_LIMIT", "ZIP decompression ratio exceeds configured limit.", sourceFile);
                }
                if (isNestedZip(sourceFile, bytes)) {
                    readZipEntries(sessionId, sourceFile, bytes, rootCompressedSize, session, parsedEvents, state, depth + 1);
                    continue;
                }
                ParsedLogFile parsed = parser.parse(sourceFile, new String(bytes, StandardCharsets.UTF_8), properties);
                session.addFileSummary(parsed.summary());
                parsedEvents.addAll(parsed.events());
                enforceEventLimit(sessionId, parsedEvents.size(), sourceFile);
            }
        }
    }

    /**
     * 扫描服务端目录日志。
     *
     * @param sessionId 会话标识
     * @param rawPath 原始目录路径
     * @return 更新后的日志分析会话
     */
    public LogAnalysisSession scanDirectory(String sessionId, String rawPath) {
        if (!properties.directoryScanEnabled()) {
            throw structuredFailure(sessionId, "DIRECTORY_SCAN_DISABLED", "Directory scan is disabled.", rawPath);
        }
        Path root = allowedDirectory(sessionId, rawPath);
        LogAnalysisSession session = getSession(sessionId);
        List<LogEvent> parsedEvents = new ArrayList<>(session.events());
        long totalBytes = 0;
        int fileCount = 0;

        try (var stream = Files.walk(root)) {
            for (Path file : stream.filter(Files::isRegularFile).toList()) {
                fileCount++;
                if (fileCount > properties.maxFiles()) {
                    throw structuredFailure(sessionId, "DIRECTORY_FILE_LIMIT", "Directory file count exceeds configured limit.", file.toString());
                }
                long fileSize = Files.size(file);
                totalBytes += fileSize;
                if (totalBytes > properties.maxUncompressedBytes()) {
                    throw structuredFailure(sessionId, "DIRECTORY_SIZE_LIMIT", "Directory content exceeds configured limit.", file.toString());
                }
                ParsedLogFile parsed = parser.parse(root.relativize(file).toString(), Files.readString(file), properties);
                session.addFileSummary(parsed.summary());
                parsedEvents.addAll(parsed.events());
                enforceEventLimit(sessionId, parsedEvents.size(), file.toString());
            }
        } catch (IOException ex) {
            throw structuredFailure(sessionId, "DIRECTORY_READ_FAILED", ex.getMessage(), rawPath);
        }
        session.addSource(new LogSource(UUID.randomUUID().toString(), LogSourceType.DIRECTORY, root.toString(), totalBytes));
        session.replaceEvents(parsedEvents);
        return repository.save(session);
    }

    /**
     * 检索已分析的日志。
     *
     * @param sessionId 会话标识
     * @param request 请求数据
     * @return 日志检索结果
     */
    /**
     * 接收 Sidecar 结构化结果并写入日志分析会话。
     *
     * @param sessionId 日志分析会话标识
     * @param request Sidecar 结构化结果
     * @return 更新后的日志分析会话
     */
    public LogAnalysisSession submitSidecarResult(String sessionId, SidecarResultSubmission request) {
        if (request == null) {
            throw structuredFailure(sessionId, "SIDECAR_RESULT_EMPTY", "Sidecar result payload is required.", null);
        }
        LogAnalysisSession session = getSession(sessionId);
        List<LogSource> sources = boundedList(request.sources(), properties.sampleLogLimit(), "SIDECAR_SOURCE_LIMIT", sessionId);
        List<LogFileSummary> summaries = boundedList(request.fileSummaries(), properties.responseLimit(), "SIDECAR_FILE_SUMMARY_LIMIT", sessionId);
        List<LogEvent> events = boundedList(request.selectedEvents(), properties.maxSearchLimit(), "SIDECAR_EVENT_LIMIT", sessionId);
        for (LogSource source : sources) {
            validateSafeSourceLabel(sessionId, source == null ? null : source.name(), "SIDECAR_SOURCE_PATH");
            if (source != null) {
                session.addSource(new LogSource(
                        source.id() == null || source.id().isBlank() ? UUID.randomUUID().toString() : TextBounds.limit(source.id(), 96),
                        source.type() == null ? LogSourceType.DIRECTORY : source.type(),
                        sanitizeLabel(source.name()),
                        Math.max(0, source.byteSize())));
            }
        }
        for (LogFileSummary summary : summaries) {
            validateSafeSourceLabel(sessionId, summary == null ? null : summary.sourceFile(), "SIDECAR_FILE_PATH");
            if (summary != null) {
                session.addFileSummary(new LogFileSummary(
                        sanitizeLabel(summary.sourceFile()),
                        Math.max(0, summary.byteSize()),
                        Math.max(0, summary.lineCount()),
                        Math.max(0, summary.eventCount()),
                        Math.max(0, summary.unparsedCount())));
            }
        }
        List<LogEvent> sanitizedEvents = new ArrayList<>(session.events());
        for (LogEvent event : events) {
            if (event == null) {
                continue;
            }
            validateSafeSourceLabel(sessionId, event.sourceFile(), "SIDECAR_EVENT_PATH");
            sanitizedEvents.add(sanitizeEvent(event));
        }
        enforceEventLimit(sessionId, sanitizedEvents.size(), "sidecar-result");
        session.replaceEvents(sanitizedEvents);
        return repository.save(session);
    }

    /**
     * 查询日志分析会话中的事件。
     *
     * @param sessionId 日志分析会话标识
     * @param request 查询过滤条件
     * @return 日志查询结果
     */
    public LogSearchResult search(String sessionId, LogSearchRequest request) {
        validateSearchRequest(request);
        LogAnalysisSession session = getSession(sessionId);
        int limit = boundedLimit(request == null ? null : request.limit());
        boolean includeStackTrace = request == null || request.includeStackTrace() == null || request.includeStackTrace();
        boolean deduplicate = request != null && Boolean.TRUE.equals(request.deduplicate());
        List<LogEvent> matched = session.events().stream()
                .filter(matches(request))
                .toList();
        List<LogEvent> searchRows = deduplicate ? deduplicate(matched) : matched;
        List<LogEvent> rows = searchRows.stream()
                .limit(limit)
                .map(event -> includeStackTrace ? event : withoutStackTrace(event))
                .toList();
        return new LogSearchResult(matched.size(), limit, rows);
    }

    /**
     * 获取日志事件聚类。
     *
     * @param sessionId 会话标识
     * @return 日志聚类结果
     */
    public List<LogCluster> clusters(String sessionId) {
        LogAnalysisSession session = getSession(sessionId);
        Map<String, List<LogEvent>> groups = session.events().stream()
                .filter(event -> event.exceptionType() != null || isImportant(event))
                .collect(Collectors.groupingBy(this::fingerprint, LinkedHashMap::new, Collectors.toList()));
        return groups.entrySet().stream()
                .map(entry -> toCluster(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(LogCluster::severity, this::severityCompare)
                        .thenComparing(LogCluster::count, Comparator.reverseOrder())
                        .thenComparing(cluster -> safeTime(cluster.firstSeen())))
                .limit(properties.responseLimit())
                .toList();
    }

    /**
     * 获取事件时间线。
     *
     * @param sessionId 会话标识
     * @return 事件时间线
     */
    public IncidentTimeline timeline(String sessionId) {
        List<LogCluster> clusters = clusters(sessionId);
        Map<String, String> clusterByFingerprint = clusters.stream()
                .collect(Collectors.toMap(LogCluster::fingerprint, LogCluster::clusterId, (left, right) -> left));
        List<TimelineEvent> events = getSession(sessionId).events().stream()
                .filter(this::isImportant)
                .sorted(Comparator.comparing(event -> safeTime(event.timestamp())))
                .limit(properties.responseLimit())
                .map(event -> new TimelineEvent(
                        event.timestamp(),
                        event.level(),
                        severity(event),
                        TextBounds.limit(summary(event), properties.rawTextLimit()),
                        event.sourceFile(),
                        event.threadName(),
                        event.traceId(),
                        clusterByFingerprint.get(fingerprint(event)),
                        event.id()))
                .toList();
        return new IncidentTimeline(sessionId, events);
    }

    /**
     * 构建证据包。
     *
     * @param sessionId 会话标识
     * @return 证据包
     */
    public EvidencePack evidencePack(String sessionId) {
        LogAnalysisSession session = getSession(sessionId);
        List<LogCluster> keyClusters = clusters(sessionId);
        List<EvidenceItem> evidence = evidenceItems(session, keyClusters);
        List<SuspectedCodeArea> suspected = suspectedCodeAreas(evidence);
        return new EvidencePack(
                sessionId,
                "Sources: " + session.getSources().size() + ", files: " + session.getFileSummaries().size()
                        + ", events: " + session.getEventCount(),
                session.getFileSummaries(),
                incidentSummary(session, keyClusters),
                keyClusters,
                timeline(sessionId),
                evidence,
                suspected,
                List.of("Which code path owns the top business stack frame?",
                        "Which configuration or dependency can produce the repeated failure?",
                        "What test would reproduce the trace-linked error path?"),
                List.of("Validate the top stack frame against the current codebase.",
                        "Check timeout, pool, retry, and circuit breaker configuration.",
                        "Run focused unit tests before broader regression tests."),
                List.of("This pack is based only on supplied logs.",
                        "Unparsed lines may indicate unsupported log formats.",
                        "Suspected code areas are heuristic and require codebase verification."));
    }

    /**
     * 构建文档格式的证据包。
     *
     * @param sessionId 会话标识
     * @return 文档格式的证据包
     */
    public String evidencePackMarkdown(String sessionId) {
        EvidencePack pack = evidencePack(sessionId);
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Evidence Pack\n\n")
                .append("## Incident Summary\n\n").append(pack.incidentSummary()).append("\n\n")
                .append("## Key Clusters\n\n");
        for (LogCluster cluster : pack.keyClusters()) {
            markdown.append("- ").append(cluster.severity()).append(" ")
                    .append(cluster.exceptionType()).append(" count=").append(cluster.count()).append("\n");
        }
        markdown.append("\n## Timeline\n\n");
        for (TimelineEvent event : pack.timeline().events()) {
            markdown.append("- ").append(event.time()).append(" ")
                    .append(event.severity()).append(" ").append(event.summary()).append("\n");
        }
        markdown.append("\n## Evidence\n\n");
        for (EvidenceItem item : pack.evidenceItems()) {
            markdown.append("- ").append(item.title()).append(": ").append(item.summary()).append("\n");
        }
        markdown.append("\n## Suspected Code Areas\n\n");
        for (SuspectedCodeArea area : pack.suspectedCodeAreas()) {
            markdown.append("- ").append(String.join(", ", area.suspectedClasses())).append(" - ")
                    .append(area.reason()).append("\n");
        }
        markdown.append("\n## Limitations\n\n");
        pack.limitations().forEach(limit -> markdown.append("- ").append(limit).append("\n"));
        return artifactSanitizer.sanitize(markdown.toString(), properties);
    }

    /**
     * 构建代码库排查任务。
     *
     * @param sessionId 会话标识
     * @return 代码库排查任务
     */
    public CodexTask codexTask(String sessionId) {
        EvidencePack pack = evidencePack(sessionId);
        String markdown = promptAssemblyService.buildCodexTaskPrompt(pack);
        return new CodexTask(sessionId, artifactSanitizer.sanitize(markdown, properties));
    }

    /**
     * 构建变更草稿。
     *
     * @param sessionId 会话标识
     * @return 变更草稿
     */
    public OpenSpecChangeDraft openSpecChangeDraft(String sessionId) {
        EvidencePack pack = evidencePack(sessionId);
        String markdown = artifactSanitizer.sanitize(promptAssemblyService.buildOpenSpecChangeDraftPrompt(pack), properties);
        return new OpenSpecChangeDraft(sessionId, markdown, "", "",
                markdown);
    }

    private void validateZipUpload(String sessionId, MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!name.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw structuredFailure(sessionId, "ZIP_UNSUPPORTED_TYPE", "Uploaded ZIP filename must end with .zip.", name);
        }
        if (!contentType.isBlank()
                && !contentType.equals("application/zip")
                && !contentType.equals("application/x-zip-compressed")
                && !contentType.equals("application/octet-stream")) {
            throw structuredFailure(sessionId, "ZIP_UNSUPPORTED_TYPE", "Uploaded ZIP content type is not supported.", name);
        }
    }

    private void validateSupportedSourceFile(String sessionId, String sourceFile) {
        String normalized = sourceFile.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".zip")
                || normalized.endsWith(".log")
                || normalized.endsWith(".txt")
                || normalized.endsWith(".out")) {
            return;
        }
        throw structuredFailure(sessionId, "UNSUPPORTED_LOG_FILE", "Only .log, .txt, .out, and nested .zip log sources are supported.", sourceFile);
    }

    private void enforceEventLimit(String sessionId, int eventCount, String sourceFile) {
        if (eventCount > properties.maxEventsPerSession()) {
            throw structuredFailure(sessionId, "LOG_EVENT_LIMIT", "Parsed log event count exceeds configured session limit.", sourceFile);
        }
    }

    private void validateSearchRequest(LogSearchRequest request) {
        if (request == null) {
            return;
        }
        if (request.timeFrom() != null && request.timeTo() != null && request.timeFrom().isAfter(request.timeTo())) {
            throw new IllegalArgumentException("Log search time range is invalid: timeFrom must be before timeTo");
        }
        if (request.keywords() != null && request.keywords().length() > properties.maxSearchKeywordLength()) {
            throw new IllegalArgumentException("Log search keywords exceed configured length limit");
        }
        List<String> fragments = keywordLines(normalize(request.keywords(), false));
        if (fragments.size() > properties.maxSearchFragments()) {
            throw new IllegalArgumentException("Log search keywords exceed configured fragment limit");
        }
        if (request.levels() != null) {
            Set<String> allowed = Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "UNPARSED");
            for (String level : request.levels()) {
                if (level == null || !allowed.contains(level.toUpperCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("Log search level is invalid");
                }
            }
        }
    }

    private <T> List<T> boundedList(List<T> values, int maxSize, String code, String sessionId) {
        if (values == null) {
            return List.of();
        }
        if (values.size() > maxSize) {
            throw structuredFailure(sessionId, code, "Sidecar result collection exceeds configured limit.", null);
        }
        return values;
    }

    private LogEvent sanitizeEvent(LogEvent event) {
        String message = artifactSanitizer.sanitize(TextBounds.limit(event.message(), properties.rawTextLimit()), properties);
        String rawText = artifactSanitizer.sanitize(TextBounds.limit(event.rawText(), properties.rawTextLimit()), properties);
        String stackTrace = artifactSanitizer.sanitize(TextBounds.limit(event.stackTrace(), properties.stackTraceLimit()), properties);
        return new LogEvent(
                event.id() == null || event.id().isBlank() ? UUID.randomUUID().toString() : TextBounds.limit(event.id(), 96),
                event.timestamp(),
                safeLevel(event.level()),
                TextBounds.limit(event.threadName(), 120),
                TextBounds.limit(event.loggerName(), 240),
                TextBounds.limit(event.traceId(), 160),
                message,
                TextBounds.limit(event.exceptionType(), 240),
                stackTrace,
                rawText,
                sanitizeLabel(event.sourceFile()),
                Math.max(0, event.lineNumber()),
                event.tags() == null ? List.of() : event.tags().stream().filter(Objects::nonNull).limit(properties.sampleLogLimit()).toList(),
                Math.max(0, event.duplicateCount()));
    }

    private String sanitizeLabel(String value) {
        return artifactSanitizer.sanitize(TextBounds.limit(value, properties.rawTextLimit()), properties);
    }

    private String safeLevel(String level) {
        if (level == null || level.isBlank()) {
            return "UNPARSED";
        }
        String normalized = level.toUpperCase(Locale.ROOT);
        return Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "UNPARSED").contains(normalized)
                ? normalized
                : "UNPARSED";
    }

    private void validateSafeSourceLabel(String sessionId, String value, String code) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (SidecarPathUtil.looksAbsolute(value) || value.contains("..")) {
            throw structuredFailure(sessionId, code, "Sidecar result must not contain absolute or traversal source paths.", value);
        }
    }

    private String safeZipEntryName(String sessionId, String entryName) {
        Path normalized = Path.of(entryName).normalize();
        if (normalized.isAbsolute() || normalized.startsWith("..") || entryName.contains("..\\")) {
            throw structuredFailure(sessionId, "ZIP_SLIP", "ZIP entry path traversal is not allowed.", entryName);
        }
        return normalized.toString().replace('\\', '/');
    }

    private boolean isNestedZip(String sourceFile, byte[] bytes) {
        return sourceFile.toLowerCase(Locale.ROOT).endsWith(".zip")
                && bytes.length >= 4
                && bytes[0] == 'P'
                && bytes[1] == 'K';
    }

    private Path allowedDirectory(String sessionId, String rawPath) {
        try {
            Path requested = Path.of(rawPath).toAbsolutePath().normalize();
            boolean allowed = properties.allowedRoots().stream()
                    .map(root -> Path.of(root).toAbsolutePath().normalize())
                    .anyMatch(requested::startsWith);
            if (!allowed) {
                throw structuredFailure(sessionId, "DIRECTORY_NOT_ALLOWED", "Directory is outside configured allowlist roots.", rawPath);
            }
            return requested;
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalArgumentException) {
                throw ex;
            }
            throw structuredFailure(sessionId, "DIRECTORY_INVALID", "Directory path is invalid.", rawPath);
        }
    }

    private IllegalArgumentException structuredFailure(String sessionId, String code, String message, String sourceFile) {
        repository.findById(sessionId).ifPresent(session -> {
            session.addError(LogAnalysisError.now(code, message, sourceFile));
            repository.save(session);
        });
        return new IllegalArgumentException(code + ": " + message);
    }

    private Predicate<LogEvent> matches(LogSearchRequest request) {
        if (request == null) {
            return event -> true;
        }
        Set<String> levels = request.levels() == null ? Set.of() : request.levels().stream()
                .filter(Objects::nonNull)
                .filter(level -> !level.isBlank())
                .map(level -> level.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
        boolean ignoreCase = request.ignoreCase() == null || request.ignoreCase();
        String keyword = normalize(request.keywords(), ignoreCase);
        List<String> keywordTokens = keywordLines(keyword);
        return event -> (request.timeFrom() == null || event.timestamp() == null || !event.timestamp().isBefore(request.timeFrom()))
                && (request.timeTo() == null || event.timestamp() == null || !event.timestamp().isAfter(request.timeTo()))
                && (levels.isEmpty() || levels.contains(upper(event.level())))
                && contains(normalize(event.traceId(), ignoreCase), normalize(request.traceId(), ignoreCase))
                && contains(normalize(event.threadName(), ignoreCase), normalize(request.threadName(), ignoreCase))
                && contains(normalize(event.loggerName(), ignoreCase), normalize(request.loggerName(), ignoreCase))
                && contains(normalize(event.exceptionType(), ignoreCase), normalize(request.exceptionType(), ignoreCase))
                && contains(normalize(event.sourceFile(), ignoreCase), normalize(request.sourceFile(), ignoreCase))
                && matchesKeywords(searchText(event, ignoreCase), keyword, keywordTokens);
    }

    private String searchText(LogEvent event, boolean ignoreCase) {
        return normalize(String.join(" ",
                nullToBlank(event.message()),
                nullToBlank(event.stackTrace()),
                nullToBlank(event.rawText()),
                nullToBlank(event.loggerName()),
                nullToBlank(event.exceptionType()),
                nullToBlank(event.traceId()),
                nullToBlank(event.threadName()),
                nullToBlank(event.sourceFile()),
                String.join(" ", event.tags() == null ? List.of() : event.tags())), ignoreCase);
    }

    private boolean matchesKeywords(String searchText, String keyword, List<String> keywordTokens) {
        if (keyword == null) {
            return true;
        }
        if (contains(searchText, keyword)) {
            return true;
        }
        return keywordTokens.stream().anyMatch(token -> contains(searchText, token));
    }

    private List<String> keywordTokens(String keyword) {
        if (keyword == null) {
            return List.of();
        }
        return Pattern.compile("[\\s,;，；]+")
                .splitAsStream(keyword)
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }

    private List<String> keywordLines(String keyword) {
        if (keyword == null) {
            return List.of();
        }
        return Pattern.compile("\\R+|\\u951B\\u5B90")
                .splitAsStream(keyword)
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }

    private int boundedLimit(Integer requested) {
        if (requested == null || requested <= 0) {
            return properties.maxSearchLimit();
        }
        return Math.min(requested, properties.maxSearchLimit());
    }

    private LogEvent withoutStackTrace(LogEvent event) {
        return new LogEvent(event.id(), event.timestamp(), event.level(), event.threadName(), event.loggerName(),
                event.traceId(), event.message(), event.exceptionType(), null, event.rawText(), event.sourceFile(),
                event.lineNumber(), event.tags(), event.duplicateCount());
    }

    private List<LogEvent> deduplicate(List<LogEvent> events) {
        Map<String, List<LogEvent>> grouped = events.stream()
                .collect(Collectors.groupingBy(this::deduplicateKey, LinkedHashMap::new, Collectors.toList()));
        return grouped.values().stream()
                .map(group -> withDuplicateCount(group.getFirst(), group.size()))
                .toList();
    }

    private LogEvent withDuplicateCount(LogEvent event, int duplicateCount) {
        return new LogEvent(event.id(), event.timestamp(), event.level(), event.threadName(), event.loggerName(),
                event.traceId(), event.message(), event.exceptionType(), event.stackTrace(), event.rawText(),
                event.sourceFile(), event.lineNumber(), event.tags(), duplicateCount);
    }

    private String deduplicateKey(LogEvent event) {
        return String.join("\n",
                nullToBlank(event.level()),
                nullToBlank(event.loggerName()),
                nullToBlank(event.exceptionType()),
                nullToBlank(event.message()),
                nullToBlank(event.stackTrace()),
                event.message() == null ? nullToBlank(event.rawText()) : "");
    }

    private LogCluster toCluster(String fingerprint, List<LogEvent> events) {
        List<LogEvent> sorted = events.stream()
                .sorted(Comparator.comparing(event -> safeTime(event.timestamp())))
                .toList();
        Set<String> classes = new HashSet<>();
        Set<String> methods = new HashSet<>();
        sorted.forEach(event -> collectCodeAreas(event.stackTrace(), classes, methods));
        String severity = sorted.stream().anyMatch(event -> "ERROR".equalsIgnoreCase(event.level())) ? "HIGH" : "MEDIUM";
        return new LogCluster(
                "CLS-" + Integer.toHexString(fingerprint.hashCode()),
                fingerprint,
                sorted.stream().map(LogEvent::exceptionType).filter(Objects::nonNull).findFirst().orElse(null),
                sorted.size(),
                sorted.getFirst().timestamp(),
                sorted.getLast().timestamp(),
                sorted.stream().limit(properties.sampleLogLimit()).map(LogEvent::id).toList(),
                sorted.stream().limit(properties.sampleLogLimit()).map(this::summary).toList(),
                distinctLimited(sorted.stream().map(LogEvent::threadName).toList()),
                distinctLimited(sorted.stream().map(LogEvent::loggerName).toList()),
                distinctLimited(new ArrayList<>(classes)),
                distinctLimited(new ArrayList<>(methods)),
                severity);
    }

    private String fingerprint(LogEvent event) {
        String normalizedMessage = lower(nullToBlank(event.message()))
                .replaceAll("\\b[0-9a-f]{8}-[0-9a-f-]{27,}\\b", "{uuid}")
                .replaceAll("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b", "{ip}")
                .replaceAll("(?i)trace[_-]?id\\s*[:=]\\s*[^\\s,;]+", "traceId={trace}")
                .replaceAll("(?i)cache[_-]?key\\s*[:=]\\s*[^\\s,;]+", "cacheKey={key}")
                .replaceAll("\\d+", "{num}");
        String topFrames = topFrames(event.stackTrace());
        return nullToBlank(event.exceptionType()) + "|" + normalizedMessage + "|" + topFrames;
    }

    private String topFrames(String stackTrace) {
        if (stackTrace == null) {
            return "";
        }
        Matcher matcher = STACK_FRAME.matcher(stackTrace);
        List<String> frames = new ArrayList<>();
        while (matcher.find() && frames.size() < 3) {
            frames.add(matcher.group(1) + "." + matcher.group(2));
        }
        return String.join("|", frames);
    }

    private List<EvidenceItem> evidenceItems(LogAnalysisSession session, List<LogCluster> clusters) {
        List<EvidenceItem> items = new ArrayList<>();
        for (LogCluster cluster : clusters.stream().limit(properties.sampleLogLimit()).toList()) {
            List<String> eventIds = cluster.sampleEventIds();
            items.add(new EvidenceItem("EVI-" + items.size(), "CLUSTER",
                    "Repeated " + nullToBlank(cluster.exceptionType()),
                    "Cluster " + cluster.clusterId() + " occurred " + cluster.count() + " times.",
                    "HIGH".equals(cluster.severity()) ? 0.9 : 0.7,
                    eventIds,
                    null,
                    TextBounds.limit(String.join("\n", cluster.sampleLogs()), properties.rawTextLimit()),
                    cluster.suspectedClasses(),
                    cluster.suspectedMethods()));
        }
        session.events().stream()
                .filter(this::isImportant)
                .limit(properties.sampleLogLimit())
                .forEach(event -> items.add(new EvidenceItem("EVI-" + items.size(), "EVENT",
                        event.level() + " log event",
                        summary(event),
                        0.75,
                        List.of(event.id()),
                        event.sourceFile(),
                        TextBounds.limit(nullToBlank(event.rawText()) + "\n" + nullToBlank(event.stackTrace()), properties.rawTextLimit()),
                        suspectedClasses(event.stackTrace(), event.loggerName()),
                        suspectedMethods(event.stackTrace()))));
        return items;
    }

    private List<SuspectedCodeArea> suspectedCodeAreas(List<EvidenceItem> evidenceItems) {
        Map<String, List<String>> evidenceByClass = new LinkedHashMap<>();
        for (EvidenceItem item : evidenceItems) {
            for (String className : item.relatedClasses()) {
                evidenceByClass.computeIfAbsent(className, ignored -> new ArrayList<>()).add(item.evidenceId());
            }
        }
        return evidenceByClass.entrySet().stream()
                .limit(properties.responseLimit())
                .map(entry -> new SuspectedCodeArea(List.of(entry.getKey()), List.of(),
                        "Appears in stack trace or logger evidence.", entry.getValue()))
                .toList();
    }

    private List<String> suspectedClasses(String stackTrace, String loggerName) {
        Set<String> classes = new HashSet<>();
        Set<String> methods = new HashSet<>();
        collectCodeAreas(stackTrace, classes, methods);
        if (isBusinessClass(loggerName)) {
            classes.add(loggerName);
        }
        return distinctLimited(new ArrayList<>(classes));
    }

    private List<String> suspectedMethods(String stackTrace) {
        Set<String> classes = new HashSet<>();
        Set<String> methods = new HashSet<>();
        collectCodeAreas(stackTrace, classes, methods);
        return distinctLimited(new ArrayList<>(methods));
    }

    private void collectCodeAreas(String stackTrace, Set<String> classes, Set<String> methods) {
        if (stackTrace == null) {
            return;
        }
        Matcher matcher = STACK_FRAME.matcher(stackTrace);
        while (matcher.find()) {
            String className = matcher.group(1);
            if (isBusinessClass(className)) {
                classes.add(className);
                methods.add(className + "." + matcher.group(2));
            }
        }
    }

    private boolean isBusinessClass(String className) {
        return className != null && EXCLUDED_CLASS_PREFIXES.stream().noneMatch(className::startsWith);
    }

    private boolean isImportant(LogEvent event) {
        String level = upper(event.level());
        String text = lower(summary(event) + " " + nullToBlank(event.stackTrace()));
        return "ERROR".equals(level)
                || "WARN".equals(level)
                || event.exceptionType() != null
                || HIGH_RISK_KEYWORDS.stream().anyMatch(text::contains);
    }

    private String severity(LogEvent event) {
        if ("ERROR".equalsIgnoreCase(event.level()) || event.exceptionType() != null) {
            return "HIGH";
        }
        return "MEDIUM";
    }

    private int severityCompare(String left, String right) {
        return Integer.compare(severityRank(left), severityRank(right));
    }

    private int severityRank(String severity) {
        return switch (nullToBlank(severity).toUpperCase(Locale.ROOT)) {
            case "HIGH" -> 0;
            case "MEDIUM" -> 1;
            default -> 2;
        };
    }

    private String incidentSummary(LogAnalysisSession session, List<LogCluster> clusters) {
        if (clusters.isEmpty()) {
            return "Parsed " + session.getEventCount() + " log events; no high-risk clusters were detected.";
        }
        LogCluster top = clusters.getFirst();
        return "Top cluster " + top.clusterId() + " contains " + top.count() + " events"
                + (top.exceptionType() == null ? "." : " for " + top.exceptionType() + ".");
    }

    private String summary(LogEvent event) {
        return TextBounds.limit(nullToBlank(event.message()).isBlank() ? nullToBlank(event.rawText()) : event.message(),
                properties.rawTextLimit());
    }

    private String evidenceBullets(List<EvidenceItem> items) {
        if (items.isEmpty()) {
            return "- No key evidence extracted.";
        }
        return items.stream().map(item -> "- " + item.title() + ": " + item.summary()).collect(Collectors.joining("\n"));
    }

    private String timelineBullets(IncidentTimeline timeline) {
        if (timeline.events().isEmpty()) {
            return "- No high-risk timeline events extracted.";
        }
        return timeline.events().stream()
                .map(event -> "- " + event.time() + " " + event.severity() + " " + event.summary())
                .collect(Collectors.joining("\n"));
    }

    private String suspectedBullets(List<SuspectedCodeArea> areas) {
        if (areas.isEmpty()) {
            return "- No suspected business code area extracted from logs.";
        }
        return areas.stream()
                .map(area -> "- " + String.join(", ", area.suspectedClasses()) + ": " + area.reason())
                .collect(Collectors.joining("\n"));
    }

    private List<String> distinctLimited(List<String> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .distinct()
                .limit(properties.sampleLogLimit())
                .toList();
    }

    private LocalDateTime safeTime(LocalDateTime time) {
        return time == null ? LocalDateTime.MIN : time;
    }

    private boolean contains(String actual, String expected) {
        return expected == null || (actual != null && actual.contains(expected));
    }

    private String lower(String value) {
        return value == null || value.isBlank() ? null : value.toLowerCase(Locale.ROOT);
    }

    private String normalize(String value, boolean ignoreCase) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ignoreCase ? value.toLowerCase(Locale.ROOT) : value;
    }

    private String upper(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private static final class ZipReadState {
        private long totalUncompressed;
        private int fileCount;
    }
}
