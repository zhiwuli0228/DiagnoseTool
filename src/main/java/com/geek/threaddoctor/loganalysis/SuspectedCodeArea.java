package com.geek.threaddoctor.loganalysis;

import java.util.List;

public record SuspectedCodeArea(
        List<String> suspectedClasses,
        List<String> suspectedMethods,
        String reason,
        List<String> relatedEvidenceIds) {
}
