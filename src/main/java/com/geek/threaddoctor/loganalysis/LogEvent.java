/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
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
    /**
     * 执行业务操作。
     *
     * @param id 记录标识
     * @param timestamp 业务参数
     * @param level 业务参数
     * @param threadName 业务参数
     * @param loggerName 业务参数
     * @param traceId 业务参数
     * @param message 消息内容
     * @param exceptionType 业务参数
     * @param stackTrace 业务参数
     * @param rawText 业务参数
     * @param sourceFile 业务参数
     * @param lineNumber 业务参数
     * @param tags 业务参数
     */
    public LogEvent(String id, LocalDateTime timestamp, String level, String threadName, String loggerName,
            String traceId, String message, String exceptionType, String stackTrace, String rawText,
            String sourceFile, int lineNumber, List<String> tags) {
        this(id, timestamp, level, threadName, loggerName, traceId, message, exceptionType, stackTrace, rawText,
                sourceFile, lineNumber, tags, 1);
    }
}
