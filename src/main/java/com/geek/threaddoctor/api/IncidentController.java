package com.geek.threaddoctor.api;

import com.geek.threaddoctor.common.SeverityLevel;
import com.geek.threaddoctor.evidence.Evidence;
import com.geek.threaddoctor.evidence.EvidenceService;
import com.geek.threaddoctor.evidence.EvidenceType;
import com.geek.threaddoctor.incident.IncidentSession;
import com.geek.threaddoctor.incident.IncidentSessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
@Validated
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
    IncidentDetailResponse get(@PathVariable @Pattern(regexp = "INC-[A-Za-z0-9-]{1,80}") String sessionId) {
        IncidentSession session = incidentSessionService.getRequired(sessionId);
        List<Evidence> evidences = evidenceService.listBySession(sessionId);
        return new IncidentDetailResponse(session, evidences);
    }

    @PostMapping("/{sessionId}/evidences")
    Evidence uploadEvidence(@PathVariable @Pattern(regexp = "INC-[A-Za-z0-9-]{1,80}") String sessionId,
            @Valid @RequestBody UploadEvidenceRequest request) {
        return evidenceService.upload(sessionId, request.type(), request.source(), request.content(), request.metadataJson());
    }

    public record CreateIncidentRequest(
            @NotBlank @Size(max = 120) String title,
            @Size(max = 2000) String description,
            SeverityLevel severity) {
        public CreateIncidentRequest {
            if (severity == null) {
                severity = SeverityLevel.MEDIUM;
            }
        }
    }

    public record UploadEvidenceRequest(
            @NotNull EvidenceType type,
            @Size(max = 200) String source,
            @NotBlank @Size(max = 200000) String content,
            @Size(max = 20000) String metadataJson) {
    }

    public record IncidentDetailResponse(IncidentSession session, List<Evidence> evidences) {
    }
}
