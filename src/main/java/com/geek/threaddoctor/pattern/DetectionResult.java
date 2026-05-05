package com.geek.threaddoctor.pattern;

import com.geek.threaddoctor.common.ConfidenceLevel;
import java.util.List;

public record DetectionResult(
        FaultPatternType type,
        boolean matched,
        ConfidenceLevel confidence,
        String summary,
        List<String> supportingEvidenceIds) {
    public static DetectionResult noMatch(FaultPatternType type) {
        return new DetectionResult(type, false, ConfidenceLevel.LOW, "No matching evidence", List.of());
    }
}
