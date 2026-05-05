package com.geek.threaddoctor.diagnosis;

import java.util.Optional;

public interface DiagnosisReportRepository {
    DiagnosisReport save(DiagnosisReport report);

    Optional<DiagnosisReport> findTopBySessionIdOrderByGeneratedAtDesc(String sessionId);
}
