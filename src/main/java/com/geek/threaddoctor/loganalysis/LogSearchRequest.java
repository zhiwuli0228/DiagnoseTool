package com.geek.threaddoctor.loganalysis;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record LogSearchRequest(
        LocalDateTime timeFrom,
        LocalDateTime timeTo,
        @Size(max = 8) List<@Size(max = 20) String> levels,
        @Size(max = 2000) String keywords,
        @Size(max = 160) String traceId,
        @Size(max = 160) String threadName,
        @Size(max = 240) String loggerName,
        @Size(max = 240) String exceptionType,
        @Size(max = 512) String sourceFile,
        @Min(1) @Max(1000) Integer limit,
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
