package com.geek.threaddoctor.loganalysis;

import java.util.List;

public record EvidenceItem(
        String evidenceId,
        String type,
        String title,
        String summary,
        double confidence,
        List<String> sourceEventIds,
        String sourceFile,
        String rawExcerpt,
        List<String> relatedClasses,
        List<String> relatedMethods) {
}
