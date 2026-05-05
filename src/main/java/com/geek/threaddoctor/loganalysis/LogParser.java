package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LogParser {
    private static final Pattern STRUCTURED = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d{3})?)\\s+([A-Z]+)\\s+(?:\\[(.*?)]\\s+)?([A-Za-z0-9_.$-]+)?\\s*-\\s*(.*)$");
    private static final Pattern EXCEPTION = Pattern.compile("([A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*(?:Exception|Error))");
    private static final Pattern TRACE_ID = Pattern.compile("(?i)(?:traceId|trace_id|X-B3-TraceId)\\s*[:=]\\s*([A-Za-z0-9._:-]+)|\\[traceId:([A-Za-z0-9._:-]+)]");
    private static final List<DateTimeFormatter> TIMESTAMP_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss,SSS", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT));

    private final SensitiveDataMasker masker;

    public LogParser(SensitiveDataMasker masker) {
        this.masker = masker;
    }

    public ParsedLogFile parse(String sourceFile, String content, LogAnalysisProperties properties) {
        String[] lines = content.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        List<LogEvent> events = new ArrayList<>();
        PendingEvent pending = null;
        int unparsed = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            Matcher matcher = STRUCTURED.matcher(line);
            if (matcher.matches()) {
                if (pending != null) {
                    events.add(pending.toEvent(properties, masker));
                }
                pending = structuredEvent(sourceFile, i + 1, line, matcher);
                continue;
            }
            if (pending != null && isStackTraceLine(line)) {
                pending.stackLines.add(line);
                continue;
            }
            if (pending != null) {
                events.add(pending.toEvent(properties, masker));
                pending = null;
            }
            unparsed++;
            String raw = masker.mask(TextBounds.limit(line, properties.rawTextLimit()), properties);
            events.add(new LogEvent(UUID.randomUUID().toString(), null, "UNPARSED", null, null,
                    extractTraceId(line), raw, null, null, raw, sourceFile, i + 1, List.of("UNPARSED")));
        }
        if (pending != null) {
            events.add(pending.toEvent(properties, masker));
        }
        return new ParsedLogFile(new LogFileSummary(sourceFile, content.getBytes().length, lines.length, events.size(), unparsed), events);
    }

    private PendingEvent structuredEvent(String sourceFile, int lineNumber, String raw, Matcher matcher) {
        String message = nullToBlank(matcher.group(5));
        return new PendingEvent(
                UUID.randomUUID().toString(),
                parseTime(matcher.group(1)),
                matcher.group(2),
                blankToNull(matcher.group(3)),
                blankToNull(matcher.group(4)),
                extractTraceId(raw),
                message,
                extractException(message),
                raw,
                sourceFile,
                lineNumber);
    }

    private LocalDateTime parseTime(String value) {
        for (DateTimeFormatter formatter : TIMESTAMP_FORMATS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // 中文注释：混合日志格式常见，继续尝试下一个时间格式。
            }
        }
        return null;
    }

    private boolean isStackTraceLine(String line) {
        String trimmed = line.stripLeading();
        return trimmed.startsWith("at ")
                || trimmed.startsWith("Caused by:")
                || trimmed.startsWith("Suppressed:")
                || trimmed.matches("\\.\\.\\. \\d+ common frames omitted")
                || EXCEPTION.matcher(trimmed).find();
    }

    static String extractTraceId(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = TRACE_ID.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    }

    static String extractException(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = EXCEPTION.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private static class PendingEvent {
        private final String id;
        private final LocalDateTime timestamp;
        private final String level;
        private final String threadName;
        private final String loggerName;
        private final String traceId;
        private final String message;
        private final String exceptionType;
        private final String rawText;
        private final String sourceFile;
        private final int lineNumber;
        private final List<String> stackLines = new ArrayList<>();

        PendingEvent(String id, LocalDateTime timestamp, String level, String threadName, String loggerName,
                String traceId, String message, String exceptionType, String rawText, String sourceFile, int lineNumber) {
            this.id = id;
            this.timestamp = timestamp;
            this.level = level;
            this.threadName = threadName;
            this.loggerName = loggerName;
            this.traceId = traceId;
            this.message = message;
            this.exceptionType = exceptionType;
            this.rawText = rawText;
            this.sourceFile = sourceFile;
            this.lineNumber = lineNumber;
        }

        LogEvent toEvent(LogAnalysisProperties properties, SensitiveDataMasker masker) {
            String stackTrace = stackLines.isEmpty() ? null : String.join("\n", stackLines);
            String detectedException = exceptionType != null ? exceptionType : LogParser.extractException(stackTrace);
            String maskedMessage = masker.mask(TextBounds.limit(message, properties.rawTextLimit()), properties);
            String maskedRaw = masker.mask(TextBounds.limit(rawText, properties.rawTextLimit()), properties);
            String maskedStack = masker.mask(TextBounds.limit(stackTrace, properties.stackTraceLimit()), properties);
            return new LogEvent(id, timestamp, level, threadName, loggerName, traceId, maskedMessage, detectedException,
                    maskedStack, maskedRaw, sourceFile, lineNumber, List.of());
        }
    }
}
