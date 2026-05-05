package com.geek.threaddoctor.diagnosis;

public enum DiagnosisProgressStep {
    PENDING,
    STARTED,
    BUILDING_CONTEXT,
    DETECTING_PATTERNS,
    GENERATING_REPORT,
    VALIDATING_REPORT,
    PERSISTING_REPORT,
    COMPLETED,
    FAILED
}
