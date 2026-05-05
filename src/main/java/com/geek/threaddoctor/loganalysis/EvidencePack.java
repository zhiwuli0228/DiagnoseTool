package com.geek.threaddoctor.loganalysis;

import java.util.List;

public record EvidencePack(
        String sessionId,
        String sourceSummary,
        List<LogFileSummary> logFileSummary,
        String incidentSummary,
        List<LogCluster> keyClusters,
        IncidentTimeline timeline,
        List<EvidenceItem> evidenceItems,
        List<SuspectedCodeArea> suspectedCodeAreas,
        List<String> recommendedCodexQuestions,
        List<String> recommendedChecks,
        List<String> limitations) {
}
