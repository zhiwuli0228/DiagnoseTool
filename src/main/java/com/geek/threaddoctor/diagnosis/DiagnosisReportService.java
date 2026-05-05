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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
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
            JsonNode parsed = parseAndValidate(llmJson);
            String summary = parsed.path("summary").asText(buildRuleSummary(detections, context.missingInformation()));
            ConfidenceLevel confidence = parseConfidence(parsed.path("confidence").asText("MEDIUM_HIGH"));
            String reportJson = buildReportJson(summary, confidence, detections, context.missingInformation(), evidenceContext);
            progressService.advance(sessionId, DiagnosisProgressStep.PERSISTING_REPORT, 95, "Caching diagnosis report.");
            DiagnosisReport report = repository.save(new DiagnosisReport("RPT-" + UUID.randomUUID(), sessionId, summary, confidence, reportJson));
            incidentSessionService.markStatus(sessionId, detections.isEmpty() ? IncidentStatus.COLLECTING_EVIDENCE : IncidentStatus.WAITING_CONFIRMATION);
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

    private JsonNode parseAndValidate(String json) {
        try {
            JsonNode node = objectMapper.readTree(extractJsonObject(json));
            if (!node.has("summary") || !node.has("confidence")) {
                throw new IllegalArgumentException("LLM output missing required fields");
            }
            parseConfidence(node.path("confidence").asText());
            return node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid LLM JSON output: " + ex.getMessage(), ex);
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

    private String buildReportJson(String summary, ConfidenceLevel confidence, List<DetectionResult> detections,
            List<String> missing, List<Map<String, Object>> evidences) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "summary", summary,
                    "confidence", confidence.name(),
                    "candidateRootCauses", detections,
                    "evidencesUsed", evidences,
                    "missingInformation", missing));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize diagnosis report", ex);
        }
    }
}
