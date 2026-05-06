/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@ConfigurationProperties(prefix = "thread-doctor.log-analysis")
public record LogAnalysisProperties(
        boolean zipEnabled,
        boolean directoryScanEnabled,
        List<String> allowedRoots,
        int maxFiles,
        long maxCompressedBytes,
        long maxUncompressedBytes,
        double maxCompressionRatio,
        int rawTextLimit,
        int stackTraceLimit,
        int sampleLogLimit,
        int responseLimit,
        int maxSearchLimit,
        int maxEventsPerSession,
        int maxZipNestingDepth,
        long maxEntryBytes,
        int maxSearchKeywordLength,
        int maxSearchFragments,
        boolean maskingEnabled) {
    public LogAnalysisProperties {
        allowedRoots = allowedRoots == null ? List.of() : List.copyOf(allowedRoots);
        maxFiles = maxFiles <= 0 ? 100 : maxFiles;
        maxCompressedBytes = maxCompressedBytes <= 0 ? 20 * 1024 * 1024L : maxCompressedBytes;
        maxUncompressedBytes = maxUncompressedBytes <= 0 ? 50 * 1024 * 1024L : maxUncompressedBytes;
        maxCompressionRatio = maxCompressionRatio <= 0 ? 20.0 : maxCompressionRatio;
        rawTextLimit = rawTextLimit <= 0 ? 2000 : rawTextLimit;
        stackTraceLimit = stackTraceLimit <= 0 ? 8000 : stackTraceLimit;
        sampleLogLimit = sampleLogLimit <= 0 ? 5 : sampleLogLimit;
        responseLimit = responseLimit <= 0 ? 200 : responseLimit;
        maxSearchLimit = maxSearchLimit <= 0 ? 200 : maxSearchLimit;
        maxEventsPerSession = maxEventsPerSession <= 0 ? 250_000 : maxEventsPerSession;
        maxZipNestingDepth = maxZipNestingDepth <= 0 ? 3 : maxZipNestingDepth;
        maxEntryBytes = maxEntryBytes <= 0 ? 20 * 1024 * 1024L : maxEntryBytes;
        maxSearchKeywordLength = maxSearchKeywordLength <= 0 ? 2000 : maxSearchKeywordLength;
        maxSearchFragments = maxSearchFragments <= 0 ? 20 : maxSearchFragments;
    }

    /**
     * 执行业务操作。
     *
     * @param zipEnabled 业务参数
     * @param directoryScanEnabled 业务参数
     * @param allowedRoots 业务参数
     * @param maxFiles 业务参数
     * @param maxCompressedBytes 业务参数
     * @param maxUncompressedBytes 业务参数
     * @param maxCompressionRatio 业务参数
     * @param rawTextLimit 业务参数
     * @param stackTraceLimit 业务参数
     * @param sampleLogLimit 业务参数
     * @param responseLimit 业务参数
     * @param maxSearchLimit 业务参数
     * @param maskingEnabled 业务参数
     */
    public LogAnalysisProperties(boolean zipEnabled, boolean directoryScanEnabled, List<String> allowedRoots,
            int maxFiles, long maxCompressedBytes, long maxUncompressedBytes, double maxCompressionRatio,
            int rawTextLimit, int stackTraceLimit, int sampleLogLimit, int responseLimit, int maxSearchLimit,
            boolean maskingEnabled) {
        this(zipEnabled, directoryScanEnabled, allowedRoots, maxFiles, maxCompressedBytes, maxUncompressedBytes,
                maxCompressionRatio, rawTextLimit, stackTraceLimit, sampleLogLimit, responseLimit, maxSearchLimit,
                250_000, 3, 20 * 1024 * 1024L, 2000, 20, maskingEnabled);
    }
}
