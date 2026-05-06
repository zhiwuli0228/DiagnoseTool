package com.geek.threaddoctor.diagnosis;

import com.geek.threaddoctor.common.ConfidenceLevel;
import java.time.LocalDateTime;
import java.util.List;

public class DiagnosisReport {
    private String id;
    private String sessionId;
    private String summary;
    private ConfidenceLevel confidence;
    private String reportJson;
    private DiagnosisLocalizationStatus localizationStatus;
    private List<String> unresolvedReasons;
    private List<FollowUpEvidenceRequest> followUpEvidenceRequests;
    private CodebaseInvestigationPrompt codebasePrompt;
    private LocalDateTime generatedAt;

    protected DiagnosisReport() {
    }

    public DiagnosisReport(String id, String sessionId, String summary, ConfidenceLevel confidence, String reportJson) {
        this(id, sessionId, summary, confidence, reportJson, DiagnosisLocalizationStatus.LOCALIZED, List.of(), List.of(), null);
    }

    public DiagnosisReport(String id, String sessionId, String summary, ConfidenceLevel confidence, String reportJson,
            DiagnosisLocalizationStatus localizationStatus, List<String> unresolvedReasons,
            List<FollowUpEvidenceRequest> followUpEvidenceRequests, CodebaseInvestigationPrompt codebasePrompt) {
        this.id = id;
        this.sessionId = sessionId;
        this.summary = summary;
        this.confidence = confidence;
        this.reportJson = reportJson;
        this.localizationStatus = localizationStatus == null ? DiagnosisLocalizationStatus.LOCALIZED : localizationStatus;
        this.unresolvedReasons = unresolvedReasons == null ? List.of() : List.copyOf(unresolvedReasons);
        this.followUpEvidenceRequests = followUpEvidenceRequests == null ? List.of() : List.copyOf(followUpEvidenceRequests);
        this.codebasePrompt = codebasePrompt;
        this.generatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getSummary() { return summary; }
    public ConfidenceLevel getConfidence() { return confidence; }
    public String getReportJson() { return reportJson; }
    public DiagnosisLocalizationStatus getLocalizationStatus() { return localizationStatus; }
    public List<String> getUnresolvedReasons() { return unresolvedReasons == null ? List.of() : List.copyOf(unresolvedReasons); }
    public List<FollowUpEvidenceRequest> getFollowUpEvidenceRequests() { return followUpEvidenceRequests == null ? List.of() : List.copyOf(followUpEvidenceRequests); }
    public CodebaseInvestigationPrompt getCodebasePrompt() { return codebasePrompt; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
}
