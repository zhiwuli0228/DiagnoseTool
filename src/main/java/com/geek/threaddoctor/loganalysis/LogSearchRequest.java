package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;
import java.util.List;

public record LogSearchRequest(
        LocalDateTime timeFrom,
        LocalDateTime timeTo,
        List<String> levels,
        String keywords,
        String traceId,
        String threadName,
        String loggerName,
        String exceptionType,
        String sourceFile,
        Integer limit,
        Boolean includeStackTrace,
        Boolean ignoreCase,
        Boolean deduplicate) {
    public LogSearchRequest(LocalDateTime timeFrom, LocalDateTime timeTo, List<String> levels, String keywords,
            String traceId, String threadName, String loggerName, String exceptionType, String sourceFile,
            Integer limit, Boolean includeStackTrace, Boolean ignoreCase) {
        this(timeFrom, timeTo, levels, keywords, traceId, threadName, loggerName, exceptionType, sourceFile,
                limit, includeStackTrace, ignoreCase, false);
    }

    public LogSearchRequest(LocalDateTime timeFrom, LocalDateTime timeTo, List<String> levels, String keywords,
            String traceId, String threadName, String loggerName, String exceptionType, String sourceFile,
            Integer limit, Boolean includeStackTrace) {
        this(timeFrom, timeTo, levels, keywords, traceId, threadName, loggerName, exceptionType, sourceFile,
                limit, includeStackTrace, true, false);
    }
}
