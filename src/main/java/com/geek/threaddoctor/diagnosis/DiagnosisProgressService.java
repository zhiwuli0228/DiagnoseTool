package com.geek.threaddoctor.diagnosis;

import org.springframework.stereotype.Service;

@Service
public class DiagnosisProgressService {
    private final DiagnosisProgressRepository repository;

    public DiagnosisProgressService(DiagnosisProgressRepository repository) {
        this.repository = repository;
    }

    public DiagnosisProgress reset(String sessionId) {
        DiagnosisProgress progress = DiagnosisProgress.notStarted(sessionId)
                .advance(DiagnosisProgressStep.STARTED, 10, "Diagnosis started.");
        return repository.save(progress);
    }

    public DiagnosisProgress advance(String sessionId, DiagnosisProgressStep step, int percent, String message) {
        DiagnosisProgress current = current(sessionId);
        return repository.save(current.advance(step, percent, message));
    }

    public DiagnosisProgress complete(String sessionId) {
        return repository.save(current(sessionId).complete("Diagnosis completed."));
    }

    public DiagnosisProgress fail(String sessionId, Exception error) {
        String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        return repository.save(current(sessionId).fail("Diagnosis failed.", message));
    }

    public DiagnosisProgress current(String sessionId) {
        return repository.findBySessionId(sessionId).orElseGet(() -> DiagnosisProgress.notStarted(sessionId));
    }
}
