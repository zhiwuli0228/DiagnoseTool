package com.geek.threaddoctor.diagnosis;

import java.util.Optional;

public interface DiagnosisProgressRepository {
    DiagnosisProgress save(DiagnosisProgress progress);

    Optional<DiagnosisProgress> findBySessionId(String sessionId);
}
