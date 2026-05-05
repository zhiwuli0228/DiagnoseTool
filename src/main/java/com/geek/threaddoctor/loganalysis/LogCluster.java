package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;
import java.util.List;

public record LogCluster(
        String clusterId,
        String fingerprint,
        String exceptionType,
        long count,
        LocalDateTime firstSeen,
        LocalDateTime lastSeen,
        List<String> sampleEventIds,
        List<String> sampleLogs,
        List<String> threadNames,
        List<String> loggerNames,
        List<String> suspectedClasses,
        List<String> suspectedMethods,
        String severity) {
}
