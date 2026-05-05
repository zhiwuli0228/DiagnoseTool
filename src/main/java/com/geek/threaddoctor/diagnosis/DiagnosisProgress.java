package com.geek.threaddoctor.diagnosis;

import java.time.LocalDateTime;

public class DiagnosisProgress {
    private final String sessionId;
    private final DiagnosisProgressStatus status;
    private final int percent;
    private final DiagnosisProgressStep step;
    private final String message;
    private final LocalDateTime startedAt;
    private final LocalDateTime updatedAt;
    private final String errorMessage;

    public DiagnosisProgress(String sessionId, DiagnosisProgressStatus status, int percent,
                             DiagnosisProgressStep step, String message, LocalDateTime startedAt,
                             LocalDateTime updatedAt, String errorMessage) {
        this.sessionId = sessionId;
        this.status = status;
        this.percent = Math.max(0, Math.min(100, percent));
        this.step = step;
        this.message = message;
        this.startedAt = startedAt;
        this.updatedAt = updatedAt;
        this.errorMessage = errorMessage;
    }

    public static DiagnosisProgress notStarted(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        return new DiagnosisProgress(sessionId, DiagnosisProgressStatus.NOT_STARTED, 0,
                DiagnosisProgressStep.PENDING, "Diagnosis has not started.", now, now, null);
    }

    public DiagnosisProgress advance(DiagnosisProgressStep nextStep, int nextPercent, String nextMessage) {
        return new DiagnosisProgress(sessionId, DiagnosisProgressStatus.RUNNING, Math.max(percent, nextPercent),
                nextStep, nextMessage, startedAt, LocalDateTime.now(), null);
    }

    public DiagnosisProgress complete(String completeMessage) {
        return new DiagnosisProgress(sessionId, DiagnosisProgressStatus.COMPLETED, 100,
                DiagnosisProgressStep.COMPLETED, completeMessage, startedAt, LocalDateTime.now(), null);
    }

    public DiagnosisProgress fail(String failedMessage, String errorMessage) {
        return new DiagnosisProgress(sessionId, DiagnosisProgressStatus.FAILED, percent,
                DiagnosisProgressStep.FAILED, failedMessage, startedAt, LocalDateTime.now(), errorMessage);
    }

    public String getSessionId() {
        return sessionId;
    }

    public DiagnosisProgressStatus getStatus() {
        return status;
    }

    public int getPercent() {
        return percent;
    }

    public DiagnosisProgressStep getStep() {
        return step;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
