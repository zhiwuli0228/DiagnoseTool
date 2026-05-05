package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;
import java.util.List;

public record LogEvent(
        String id,
        LocalDateTime timestamp,
        String level,
        String threadName,
        String loggerName,
        String traceId,
        String message,
        String exceptionType,
        String stackTrace,
        String rawText,
        String sourceFile,
        int lineNumber,
        List<String> tags,
        int duplicateCount) {
    public LogEvent(String id, LocalDateTime timestamp, String level, String threadName, String loggerName,
            String traceId, String message, String exceptionType, String stackTrace, String rawText,
            String sourceFile, int lineNumber, List<String> tags) {
        this(id, timestamp, level, threadName, loggerName, traceId, message, exceptionType, stackTrace, rawText,
                sourceFile, lineNumber, tags, 1);
    }
}
