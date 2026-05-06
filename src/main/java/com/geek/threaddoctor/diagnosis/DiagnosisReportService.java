package com.geek.threaddoctor.diagnosis;

import com.geek.threaddoctor.common.ConfidenceLevel;
import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.evidence.Evidence;
import com.geek.threaddoctor.incident.IncidentStatus;
import com.geek.threaddoctor.incident.IncidentSessionService;
import com.geek.threaddoctor.llm.LlmClient;
import com.geek.threaddoctor.llm.LlmRequest;
import com.geek.threaddoctor.pattern.DetectionResult;
import com.geek.threaddoctor.pattern.DiagnosisContext;
import com.geek.threaddoctor.pattern.FaultPatternDetectionService;
import com.geek.threaddoctor.prompt.DiagnosisPrompt;
import com.geek.threaddoctor.prompt.PromptAssemblyService;
import com.geek.threaddoctor.security.SensitiveValueSanitizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisReportService {
    private final DiagnosisContextBuilder contextBuilder;
    private final FaultPatternDetectionService detectionService;
    private final DiagnosisReportRepository repository;
    private final IncidentSessionService incidentSessionService;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final DiagnosisProgressService progressService;
    private final PromptAssemblyService promptAssemblyService;
    private final SensitiveValueSanitizer sensitiveValueSanitizer = new SensitiveValueSanitizer();

    public DiagnosisReportService(DiagnosisContextBuilder contextBuilder, FaultPatternDetectionService detectionService,
                                  DiagnosisReportRepository repository, IncidentSessionService incidentSessionService,
                                  LlmClient llmClient, ObjectMapper objectMapper,
                                  DiagnosisProgressService progressService,
                                  PromptAssemblyService promptAssemblyService) {
        this.contextBuilder = contextBuilder;
        this.detectionService = detectionService;
        this.repository = repository;
        this.incidentSessionService = incidentSessionService;
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        this.progressService = progressService;
        this.promptAssemblyService = promptAssemblyService;
    }

    public DiagnosisReport diagnose(String sessionId) {
        progressService.reset(sessionId);
        try {
            incidentSessionService.markStatus(sessionId, IncidentStatus.DIAGNOSING);
            progressService.advance(sessionId, DiagnosisProgressStep.BUILDING_CONTEXT, 25, "Building diagnosis context.");
            DiagnosisContext context = contextBuilder.build(sessionId);
            progressService.advance(sessionId, DiagnosisProgressStep.DETECTING_PATTERNS, 45, "Detecting fault patterns.");
            List<DetectionResult> detections = detectionService.detect(context);
            progressService.advance(sessionId, DiagnosisProgressStep.GENERATING_REPORT, 70, "Generating diagnosis report.");
            List<Map<String, Object>> evidenceContext = evidenceContext(context.evidences());
            DiagnosisPrompt prompt = promptAssemblyService.buildDiagnosisPrompt(context, detections, evidenceContext);
            String llmJson = llmClient.complete(new LlmRequest(prompt.systemPrompt() + "\n\n" + prompt.userPrompt(), Map.of(
                    "incident", Map.of(
                            "id", context.session().getId(),
                            "title", context.session().getTitle(),
                            "description", context.session().getDescription(),
                            "severity", context.session().getSeverity()),
                    "evidences", evidenceContext,
                    "detections", detections,
                    "missingInformation", context.missingInformation(),
                    "jsonSchema", prompt.jsonSchema()), 0.2, 2000)).content();
            progressService.advance(sessionId, DiagnosisProgressStep.VALIDATING_REPORT, 90, "Validating diagnosis report.");
            DiagnosisResult result = buildDiagnosisResult(llmJson, context, detections, evidenceContext);
            String reportJson = buildReportJson(result, detections, context.missingInformation(), evidenceContext);
            progressService.advance(sessionId, DiagnosisProgressStep.PERSISTING_REPORT, 95, "Caching diagnosis report.");
            DiagnosisReport report = repository.save(new DiagnosisReport("RPT-" + UUID.randomUUID(), sessionId, result.summary(),
                    result.confidence(), reportJson, result.localizationStatus(), result.unresolvedReasons(),
                    result.followUpEvidenceRequests(), result.codebasePrompt()));
            incidentSessionService.markStatus(sessionId,
                    result.localizationStatus() == DiagnosisLocalizationStatus.NEEDS_MORE_EVIDENCE
                            ? IncidentStatus.COLLECTING_EVIDENCE
                            : IncidentStatus.WAITING_CONFIRMATION);
            progressService.complete(sessionId);
            return report;
        } catch (RuntimeException ex) {
            progressService.fail(sessionId, ex);
            throw ex;
        }
    }

    public DiagnosisReport latest(String sessionId) {
        return repository.findTopBySessionIdOrderByGeneratedAtDesc(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis report not found for session: " + sessionId));
    }

    private DiagnosisResult buildDiagnosisResult(String llmJson, DiagnosisContext context, List<DetectionResult> detections,
            List<Map<String, Object>> evidenceContext) {
        List<String> fallbackReasons = new ArrayList<>();
        JsonNode parsed = parseLlmJson(llmJson, fallbackReasons);
        String summary = parsed == null
                ? buildRuleSummary(detections, context.missingInformation())
                : parsed.path("summary").asText(buildRuleSummary(detections, context.missingInformation()));
        ConfidenceLevel confidence = parsed == null ? ConfidenceLevel.LOW : parseConfidenceOrDefault(parsed.path("confidence").asText(), ConfidenceLevel.MEDIUM);
        DiagnosisLocalizationStatus status = determineStatus(parsed, detections, context.missingInformation(), fallbackReasons);
        List<String> unresolvedReasons = unresolvedReasons(parsed, fallbackReasons, detections, context.missingInformation(), status);
        List<FollowUpEvidenceRequest> followUps = followUpEvidenceRequests(parsed, context.missingInformation());
        CodebaseInvestigationPrompt prompt = status == DiagnosisLocalizationStatus.LOCALIZED ? null
                : buildCodebasePrompt(context, detections, evidenceContext, summary, confidence, status, unresolvedReasons, followUps);
        return new DiagnosisResult(summary, confidence, status, unresolvedReasons, followUps, prompt);
    }

    private JsonNode parseLlmJson(String json, List<String> fallbackReasons) {
        try {
            JsonNode node = objectMapper.readTree(extractJsonObject(json));
            if (!node.has("summary") || !node.has("confidence")) {
                fallbackReasons.add("LLM output did not provide required summary/confidence fields.");
            }
            if (!node.has("localizationStatus")) {
                fallbackReasons.add("LLM output did not provide localizationStatus.");
            }
            parseConfidenceOrDefault(node.path("confidence").asText(), ConfidenceLevel.MEDIUM);
            return node;
        } catch (Exception ex) {
            fallbackReasons.add("LLM output could not be parsed as valid diagnosis JSON: " + ex.getMessage());
            return null;
        }
    }

    private String extractJsonObject(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("LLM output is empty");
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("LLM output does not contain a JSON object");
        }
        return trimmed.substring(start, end + 1);
    }

    private ConfidenceLevel parseConfidence(String value) {
        return ConfidenceLevel.valueOf(value);
    }

    private ConfidenceLevel parseConfidenceOrDefault(String value, ConfidenceLevel fallback) {
        try {
            return parseConfidence(value);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private DiagnosisLocalizationStatus determineStatus(JsonNode parsed, List<DetectionResult> detections,
            List<String> missingInformation, List<String> fallbackReasons) {
        if (parsed != null && parsed.hasNonNull("localizationStatus")) {
            try {
                return DiagnosisLocalizationStatus.valueOf(parsed.path("localizationStatus").asText());
            } catch (RuntimeException ex) {
                fallbackReasons.add("LLM output provided an unknown localizationStatus.");
            }
        }
        if (!missingInformation.isEmpty()) {
            return DiagnosisLocalizationStatus.NEEDS_MORE_EVIDENCE;
        }
        return detections.isEmpty() || !fallbackReasons.isEmpty()
                ? DiagnosisLocalizationStatus.UNRESOLVED
                : DiagnosisLocalizationStatus.LOCALIZED;
    }

    private List<String> unresolvedReasons(JsonNode parsed, List<String> fallbackReasons, List<DetectionResult> detections,
            List<String> missingInformation, DiagnosisLocalizationStatus status) {
        if (status == DiagnosisLocalizationStatus.LOCALIZED) {
            return List.of();
        }
        Set<String> reasons = new LinkedHashSet<>(fallbackReasons);
        if (parsed != null && parsed.has("unresolvedReasons")) {
            parsed.path("unresolvedReasons").forEach(item -> addNonBlank(reasons, item.asText()));
        }
        missingInformation.forEach(reason -> addNonBlank(reasons, reason));
        if (detections.isEmpty()) {
            reasons.add("No deterministic fault pattern matched the current evidence.");
        }
        if (reasons.isEmpty()) {
            reasons.add("The current evidence does not localize a concrete code or configuration root cause.");
        }
        return List.copyOf(reasons).stream().limit(8).toList();
    }

    private void addNonBlank(Set<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(limit(sensitiveValueSanitizer.sanitize(value), 300));
        }
    }

    private List<FollowUpEvidenceRequest> followUpEvidenceRequests(JsonNode parsed, List<String> missingInformation) {
        List<FollowUpEvidenceRequest> requests = new ArrayList<>();
        if (parsed != null && parsed.has("followUpEvidenceRequests")) {
            parsed.path("followUpEvidenceRequests").forEach(item -> {
                String title = item.path("title").asText("");
                String reason = item.path("reason").asText("");
                if (!title.isBlank() || !reason.isBlank()) {
                    requests.add(new FollowUpEvidenceRequest(
                            limit(sensitiveValueSanitizer.sanitize(title.isBlank() ? "Provide more evidence" : title), 120),
                            limit(sensitiveValueSanitizer.sanitize(reason), 300),
                            limit(sensitiveValueSanitizer.sanitize(item.path("expectedFormat").asText("Plain text, JSON, logs, or jstack snippet")), 160),
                            limit(sensitiveValueSanitizer.sanitize(item.path("guidance").asText("Submit the requested evidence and run diagnosis again.")), 300)));
                }
            });
        }
        if (requests.isEmpty()) {
            missingInformation.forEach(missing -> requests.add(defaultFollowUp(missing)));
        }
        return requests.stream().limit(5).toList();
    }

    private FollowUpEvidenceRequest defaultFollowUp(String missing) {
        String lower = missing == null ? "" : missing.toLowerCase();
        if (lower.contains("jstack") || lower.contains("thread")) {
            return new FollowUpEvidenceRequest("Provide jstack thread dump", missing, "jstack text",
                    "Capture jstack during the failure window and submit the relevant blocked/runnable thread section.");
        }
        if (lower.contains("metric") || lower.contains("redis") || lower.contains("jvm") || lower.contains("kafka") || lower.contains("db")) {
            return new FollowUpEvidenceRequest("Provide metrics snapshot", missing, "JVM/Redis/Kafka/DB metrics JSON",
                    "Submit metrics from the same time window as the incident.");
        }
        if (lower.contains("log") || lower.contains("trace")) {
            return new FollowUpEvidenceRequest("Provide key logs or trace ID", missing, "Log snippet with timestamp and traceId",
                    "Submit bounded logs around the failure timestamp, including stack traces when available.");
        }
        return new FollowUpEvidenceRequest("Provide missing evidence", missing, "Plain text or JSON",
                "Submit the missing context and run diagnosis again.");
    }

    private CodebaseInvestigationPrompt buildCodebasePrompt(DiagnosisContext context, List<DetectionResult> detections,
            List<Map<String, Object>> evidenceContext, String summary, ConfidenceLevel confidence,
            DiagnosisLocalizationStatus status, List<String> unresolvedReasons, List<FollowUpEvidenceRequest> followUps) {
        String markdown = promptAssemblyService.buildDiagnosisCodebaseInvestigationPrompt(Map.of(
                "incidentSummary", sanitizeAndLimit(context.session().getTitle() + " - " + context.session().getDescription(), 1200),
                "diagnosisSummary", sanitizeAndLimit(summary, 1000),
                "confidence", confidence.name(),
                "localizationStatus", status.name(),
                "unresolvedReasons", bulletList(unresolvedReasons, "- No unresolved reason recorded."),
                "evidenceSummary", evidenceSummary(evidenceContext),
                "detectionSummary", detectionSummary(detections),
                "missingInformation", missingInformationSummary(context.missingInformation(), followUps)));
        String warning = "Document-only prompt for Codex/OpenCode handoff. Thread Doctor does not execute it.";
        return new CodebaseInvestigationPrompt(sanitizeAndLimit(markdown, 12000), warning);
    }

    private String evidenceSummary(List<Map<String, Object>> evidenceContext) {
        if (evidenceContext.isEmpty()) {
            return "- No submitted evidence.";
        }
        return evidenceContext.stream()
                .limit(8)
                .map(item -> "- " + item.get("id") + " [" + item.get("type") + "] " + sanitizeAndLimit(String.valueOf(item.get("content")), 800))
                .toList()
                .stream()
                .reduce((left, right) -> left + "\n" + right)
                .orElse("- No submitted evidence.");
    }

    private String detectionSummary(List<DetectionResult> detections) {
        if (detections.isEmpty()) {
            return "- No deterministic pattern matched.";
        }
        return detections.stream()
                .limit(8)
                .map(detection -> "- " + detection.type().name() + " confidence=" + detection.confidence().name()
                        + " reason=" + sanitizeAndLimit(detection.summary(), 300))
                .toList()
                .stream()
                .reduce((left, right) -> left + "\n" + right)
                .orElse("- No deterministic pattern matched.");
    }

    private String missingInformationSummary(List<String> missingInformation, List<FollowUpEvidenceRequest> followUps) {
        if (!missingInformation.isEmpty()) {
            return bulletList(missingInformation, "- No missing evidence recorded.");
        }
        return bulletList(followUps.stream().map(FollowUpEvidenceRequest::title).toList(), "- No missing evidence recorded.");
    }

    private String bulletList(List<String> values, String emptyValue) {
        if (values == null || values.isEmpty()) {
            return emptyValue;
        }
        return values.stream()
                .limit(8)
                .map(value -> "- " + sanitizeAndLimit(value, 300))
                .toList()
                .stream()
                .reduce((left, right) -> left + "\n" + right)
                .orElse(emptyValue);
    }

    private String sanitizeAndLimit(String value, int maxLength) {
        return limit(sensitiveValueSanitizer.sanitize(value == null ? "" : value), maxLength);
    }

    private String buildRuleSummary(List<DetectionResult> detections, List<String> missing) {
        if (detections.isEmpty()) {
            return "Evidence is insufficient for a confident diagnosis. Missing information: " + missing;
        }
        return "Detected candidate fault patterns: " + detections.stream().map(d -> d.type().name()).toList();
    }

    private List<Map<String, Object>> evidenceContext(List<Evidence> evidences) {
        return evidences.stream()
                .map(evidence -> Map.<String, Object>of(
                        "id", evidence.getId(),
                        "type", evidence.getType(),
                        "source", evidence.getSource() == null ? "" : evidence.getSource(),
                        // 中文注释：只传有限长度的原始证据，避免日志过长导致请求不可控。
                        "content", limit(evidence.getContent(), 4000),
                        "metadataJson", evidence.getMetadataJson() == null ? "" : evidence.getMetadataJson()))
                .toList();
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...[truncated]";
    }

    private String buildReportJson(DiagnosisResult result, List<DetectionResult> detections,
            List<String> missing, List<Map<String, Object>> evidences) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "summary", result.summary(),
                    "confidence", result.confidence().name(),
                    "localizationStatus", result.localizationStatus().name(),
                    "unresolvedReasons", result.unresolvedReasons(),
                    "followUpEvidenceRequests", result.followUpEvidenceRequests(),
                    "codebasePrompt", result.codebasePrompt() == null ? Map.of() : result.codebasePrompt(),
                    "candidateRootCauses", detections,
                    "evidencesUsed", evidences,
                    "missingInformation", missing));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize diagnosis report", ex);
        }
    }

    private record DiagnosisResult(
            String summary,
            ConfidenceLevel confidence,
            DiagnosisLocalizationStatus localizationStatus,
            List<String> unresolvedReasons,
            List<FollowUpEvidenceRequest> followUpEvidenceRequests,
            CodebaseInvestigationPrompt codebasePrompt) {
    }
}
