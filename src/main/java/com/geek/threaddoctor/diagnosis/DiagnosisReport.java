package com.geek.threaddoctor.diagnosis;

import com.geek.threaddoctor.common.ConfidenceLevel;
import java.time.LocalDateTime;

public class DiagnosisReport {
    private String id;
    private String sessionId;
    private String summary;
    private ConfidenceLevel confidence;
    private String reportJson;
    private LocalDateTime generatedAt;

    protected DiagnosisReport() {
    }

    public DiagnosisReport(String id, String sessionId, String summary, ConfidenceLevel confidence, String reportJson) {
        this.id = id;
        this.sessionId = sessionId;
        this.summary = summary;
        this.confidence = confidence;
        this.reportJson = reportJson;
        this.generatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getSummary() { return summary; }
    public ConfidenceLevel getConfidence() { return confidence; }
    public String getReportJson() { return reportJson; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
}
