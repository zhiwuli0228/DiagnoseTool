package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;

public record LogAnalysisError(String code, String message, String sourceFile, LocalDateTime occurredAt) {
    public static LogAnalysisError now(String code, String message, String sourceFile) {
        return new LogAnalysisError(code, message, sourceFile, LocalDateTime.now());
    }
}
