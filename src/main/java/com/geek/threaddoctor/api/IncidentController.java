package com.geek.threaddoctor.api;

import com.geek.threaddoctor.common.SeverityLevel;
import com.geek.threaddoctor.evidence.Evidence;
import com.geek.threaddoctor.evidence.EvidenceService;
import com.geek.threaddoctor.evidence.EvidenceType;
import com.geek.threaddoctor.incident.IncidentSession;
import com.geek.threaddoctor.incident.IncidentSessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentSessionService incidentSessionService;
    private final EvidenceService evidenceService;

    public IncidentController(IncidentSessionService incidentSessionService, EvidenceService evidenceService) {
        this.incidentSessionService = incidentSessionService;
        this.evidenceService = evidenceService;
    }

    @PostMapping
    IncidentSession create(@Valid @RequestBody CreateIncidentRequest request) {
        return incidentSessionService.create(request.title(), request.description(), request.severity());
    }

    @GetMapping("/{sessionId}")
    IncidentDetailResponse get(@PathVariable String sessionId) {
        IncidentSession session = incidentSessionService.getRequired(sessionId);
        List<Evidence> evidences = evidenceService.listBySession(sessionId);
        return new IncidentDetailResponse(session, evidences);
    }

    @PostMapping("/{sessionId}/evidences")
    Evidence uploadEvidence(@PathVariable String sessionId, @Valid @RequestBody UploadEvidenceRequest request) {
        return evidenceService.upload(sessionId, request.type(), request.source(), request.content(), request.metadataJson());
    }

    public record CreateIncidentRequest(@NotBlank String title, String description, SeverityLevel severity) {
        public CreateIncidentRequest {
            if (severity == null) {
                severity = SeverityLevel.MEDIUM;
            }
        }
    }

    public record UploadEvidenceRequest(EvidenceType type, String source, @NotBlank String content, String metadataJson) {
    }

    public record IncidentDetailResponse(IncidentSession session, List<Evidence> evidences) {
    }
}
