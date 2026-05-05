package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;

public record TimelineEvent(
        LocalDateTime time,
        String eventType,
        String severity,
        String summary,
        String sourceFile,
        String threadName,
        String traceId,
        String relatedClusterId,
        String evidenceEventId) {
}
