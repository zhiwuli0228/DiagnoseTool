package com.geek.threaddoctor.pattern;

public interface FaultPatternDetector {
    FaultPatternType type();

    DetectionResult detect(DiagnosisContext context);
}
