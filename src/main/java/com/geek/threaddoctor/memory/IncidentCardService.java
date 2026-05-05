package com.geek.threaddoctor.memory;

import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.diagnosis.DiagnosisReportService;
import com.geek.threaddoctor.loganalysis.EvidencePack;
import com.geek.threaddoctor.loganalysis.IncidentTimeline;
import com.geek.threaddoctor.prompt.PromptAssemblyService;
import com.geek.threaddoctor.recovery.RecoveryAction;
import com.geek.threaddoctor.recovery.RecoveryActionService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class IncidentCardService {
    private final IncidentCardRepository repository;
    private final DiagnosisReportService diagnosisReportService;
    private final RecoveryActionService recoveryActionService;
    private final PromptAssemblyService promptAssemblyService;

    public IncidentCardService(IncidentCardRepository repository, DiagnosisReportService diagnosisReportService,
            RecoveryActionService recoveryActionService, PromptAssemblyService promptAssemblyService) {
        this.repository = repository;
        this.diagnosisReportService = diagnosisReportService;
        this.recoveryActionService = recoveryActionService;
        this.promptAssemblyService = promptAssemblyService;
    }

    public IncidentCard generate(String sessionId) {
        DiagnosisReport report = diagnosisReportService.latest(sessionId);
        List<RecoveryAction> actions = recoveryActionService.list(sessionId);
        String tags = tags(report.getReportJson());
        EvidencePack pack = incidentReviewPack(sessionId, report, actions);
        String markdown = promptAssemblyService.buildIncidentReviewPrompt(pack, report);
        return repository.save(new IncidentCard("CARD-" + UUID.randomUUID(), sessionId, markdown, tags));
    }

    public IncidentCard latest(String sessionId) {
        return repository.findTopBySessionIdOrderByCreatedAtDesc(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident result document not found for session: " + sessionId));
    }

    private String tags(String reportJson) {
        if (reportJson != null && reportJson.contains("REDIS_POOL_EXHAUSTED")) {
            return "REDIS_POOL_EXHAUSTED";
        }
        if (reportJson != null && reportJson.contains("LOCK_CONTENTION")) {
            return "LOCK_CONTENTION";
        }
        return "INSUFFICIENT_EVIDENCE";
    }

    private EvidencePack incidentReviewPack(String sessionId, DiagnosisReport report, List<RecoveryAction> actions) {
        return new EvidencePack(
                sessionId,
                "Diagnosis report and recovery actions",
                List.of(),
                report.getSummary(),
                List.of(),
                new IncidentTimeline(sessionId, List.of()),
                List.of(),
                List.of(),
                List.of("Review whether monitoring and runbooks need updates."),
                actions.stream().map(RecoveryAction::getTitle).toList(),
                List.of(report.getReportJson()));
    }
}
