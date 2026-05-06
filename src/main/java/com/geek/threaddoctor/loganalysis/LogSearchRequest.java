/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
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
    /**
     * 执行业务操作。
     *
     * @param timeFrom 业务参数
     * @param timeTo 业务参数
     * @param levels 业务参数
     * @param keywords 业务参数
     * @param traceId 业务参数
     * @param threadName 业务参数
     * @param loggerName 业务参数
     * @param exceptionType 业务参数
     * @param sourceFile 业务参数
     * @param limit 业务参数
     * @param includeStackTrace 业务参数
     * @param ignoreCase 业务参数
     */
    public LogSearchRequest(LocalDateTime timeFrom, LocalDateTime timeTo, List<String> levels, String keywords,
            String traceId, String threadName, String loggerName, String exceptionType, String sourceFile,
            Integer limit, Boolean includeStackTrace, Boolean ignoreCase) {
        this(timeFrom, timeTo, levels, keywords, traceId, threadName, loggerName, exceptionType, sourceFile,
                limit, includeStackTrace, ignoreCase, false);
    }

    /**
     * 执行业务操作。
     *
     * @param timeFrom 业务参数
     * @param timeTo 业务参数
     * @param levels 业务参数
     * @param keywords 业务参数
     * @param traceId 业务参数
     * @param threadName 业务参数
     * @param loggerName 业务参数
     * @param exceptionType 业务参数
     * @param sourceFile 业务参数
     * @param limit 业务参数
     * @param includeStackTrace 业务参数
     */
    public LogSearchRequest(LocalDateTime timeFrom, LocalDateTime timeTo, List<String> levels, String keywords,
            String traceId, String threadName, String loggerName, String exceptionType, String sourceFile,
            Integer limit, Boolean includeStackTrace) {
        this(timeFrom, timeTo, levels, keywords, traceId, threadName, loggerName, exceptionType, sourceFile,
                limit, includeStackTrace, true, false);
    }
}
