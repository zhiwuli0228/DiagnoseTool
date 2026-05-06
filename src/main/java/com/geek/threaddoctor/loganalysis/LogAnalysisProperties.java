package com.geek.threaddoctor.loganalysis;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

    public LogAnalysisProperties(boolean zipEnabled, boolean directoryScanEnabled, List<String> allowedRoots,
            int maxFiles, long maxCompressedBytes, long maxUncompressedBytes, double maxCompressionRatio,
            int rawTextLimit, int stackTraceLimit, int sampleLogLimit, int responseLimit, int maxSearchLimit,
            boolean maskingEnabled) {
        this(zipEnabled, directoryScanEnabled, allowedRoots, maxFiles, maxCompressedBytes, maxUncompressedBytes,
                maxCompressionRatio, rawTextLimit, stackTraceLimit, sampleLogLimit, responseLimit, maxSearchLimit,
                250_000, 3, 20 * 1024 * 1024L, 2000, 20, maskingEnabled);
    }
}
