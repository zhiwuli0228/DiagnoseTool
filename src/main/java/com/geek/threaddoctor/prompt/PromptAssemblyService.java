/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.loganalysis.EvidenceItem;
import com.geek.threaddoctor.loganalysis.EvidencePack;
import com.geek.threaddoctor.loganalysis.IncidentTimeline;
import com.geek.threaddoctor.loganalysis.SuspectedCodeArea;
import com.geek.threaddoctor.loganalysis.TimelineEvent;
import com.geek.threaddoctor.pattern.DetectionResult;
import com.geek.threaddoctor.pattern.DiagnosisContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Service
public class PromptAssemblyService {
    private final PromptTemplateLoader loader;
    private final PromptRenderer renderer;
    private final PromptProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * 执行业务操作。
     *
     * @param loader 业务参数
     * @param renderer 业务参数
     * @param properties 配置属性
     * @param objectMapper 数据映射组件
     */
    public PromptAssemblyService(PromptTemplateLoader loader, PromptRenderer renderer,
            PromptProperties properties, ObjectMapper objectMapper) {
        this.loader = loader;
        this.renderer = renderer;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行业务操作。
     *
     * @param evidencePack 业务参数
     * @param request 请求数据
     * @return 业务处理结果
     */
    public DiagnosisPrompt buildDiagnosisPrompt(EvidencePack evidencePack, DiagnosisRequest request) {
        return buildDiagnosisPrompt(
                request.userGoal(),
                request.incidentContext(),
                toJson(Map.of(
                        "sessionId", evidencePack.sessionId(),
                        "incidentSummary", evidencePack.incidentSummary(),
                        "evidenceItems", evidencePack.evidenceItems(),
                        "suspectedCodeAreas", evidencePack.suspectedCodeAreas(),
                        "recommendedChecks", evidencePack.recommendedChecks())));
    }

    /**
     * 执行业务操作。
     *
     * @param context 诊断上下文
     * @param detections 业务参数
     * @param evidenceContext 业务参数
     * @return 业务处理结果
     */
    public DiagnosisPrompt buildDiagnosisPrompt(DiagnosisContext context, List<DetectionResult> detections,
            List<Map<String, Object>> evidenceContext) {
        Map<String, Object> incident = Map.of(
                "id", context.session().getId(),
                "title", context.session().getTitle(),
                "description", context.session().getDescription(),
                "severity", context.session().getSeverity());
        Map<String, Object> evidencePack = Map.of(
                "incident", incident,
                "evidences", evidenceContext,
                "detections", detections,
                "missingInformation", context.missingInformation());
        return buildDiagnosisPrompt(
                context.session().getTitle(),
                toJson(incident),
                toJson(evidencePack));
    }

    /**
     * 执行业务操作。
     *
     * @param evidencePack 业务参数
     * @return 文本结果
     */
    public String buildCodexTaskPrompt(EvidencePack evidencePack) {
        return render(PromptTemplateType.CODEX_INVESTIGATION_TASK, Map.of(
                "incidentSummary", evidencePack.incidentSummary(),
                "keyEvidence", evidenceBullets(evidencePack.evidenceItems()),
                "timeline", timelineBullets(evidencePack.timeline()),
                "suspectedCodeAreas", suspectedBullets(evidencePack.suspectedCodeAreas())));
    }

    /**
     * 执行业务操作。
     *
     * @param variables 业务参数
     * @return 文本结果
     */
    public String buildDiagnosisCodebaseInvestigationPrompt(Map<String, Object> variables) {
        return render(PromptTemplateType.DIAGNOSIS_CODEBASE_INVESTIGATION, variables);
    }

    /**
     * 执行业务操作。
     *
     * @param evidencePack 业务参数
     * @return 文本结果
     */
    public String buildOpenSpecChangeDraftPrompt(EvidencePack evidencePack) {
        String suspected = suspectedBullets(evidencePack.suspectedCodeAreas());
        String tasks = """
                ## 1. Investigation

                - [ ] 1.1 Verify the evidence against current code.
                - [ ] 1.2 Confirm or reject the suspected root cause.

                ## 2. Implementation

                - [ ] 2.1 Apply the smallest confirmed fix.
                - [ ] 2.2 Add JUnit 5 and Mockito tests.
                - [ ] 2.3 Run `mvn test`.
                """;
        String specDelta = """
                ## ADDED Requirements

                ### Requirement: Verified incident fix
                The system SHALL address the root cause only after it is verified against code evidence.

                #### Scenario: Root cause verified
                - **WHEN** the suspected root cause is confirmed in code
                - **THEN** the implementation changes only the affected behavior and includes regression tests
                """;
        return render(PromptTemplateType.OPENSPEC_CHANGE_DRAFT, Map.of(
                "why", evidencePack.incidentSummary(),
                "whatChanges", "- Address the verified root cause indicated by the evidence pack.\n- Add focused tests for the confirmed failure path.",
                "impact", "- Suspected code areas: " + suspected,
                "risk", "Behavior change must be limited to verified root cause.",
                "rollback", "Revert the focused change if validation fails.",
                "acceptanceCriteria", "- Root cause is verified against code evidence.\n- Regression tests cover the failure path.",
                "tests", "- Run `mvn test`.",
                "tasks", tasks,
                "specDelta", specDelta));
    }

    /**
     * 执行业务操作。
     *
     * @param evidencePack 业务参数
     * @param diagnosisReport 业务参数
     * @return 文本结果
     */
    public String buildIncidentReviewPrompt(EvidencePack evidencePack, DiagnosisReport diagnosisReport) {
        return render(PromptTemplateType.INCIDENT_REVIEW, Map.of(
                "faultName", diagnosisReport.getSummary(),
                "symptoms", evidencePack.incidentSummary(),
                "impact", "See incident context and submitted evidence.",
                "diagnosisConclusion", diagnosisReport.getSummary(),
                "keyEvidence", evidenceBullets(evidencePack.evidenceItems()),
                "timeline", timelineBullets(evidencePack.timeline()),
                "recoveryActions", String.join("\n", evidencePack.recommendedChecks()),
                "verificationResult", "Review action verification fields and simulated execution result.",
                "preventionSuggestions", String.join("\n", evidencePack.recommendedCodexQuestions()),
                "relatedFaultPatterns", diagnosisReport.getReportJson()));
    }

    private DiagnosisPrompt buildDiagnosisPrompt(String userGoal, String incidentContext, String evidencePackJson) {
        String systemPrompt = render(PromptTemplateType.DIAGNOSIS_SYSTEM_PROMPT, Map.of(
                "defaultOutputLanguage", properties.defaultOutputLanguage()));
        String userPrompt = render(PromptTemplateType.DIAGNOSIS_USER_PROMPT, Map.of(
                "userGoal", userGoal,
                "incidentContext", incidentContext,
                "evidencePackJson", evidencePackJson));
        String jsonSchema = load(PromptTemplateType.DIAGNOSIS_JSON_SCHEMA).content();
        validateJson(PromptTemplateType.DIAGNOSIS_JSON_SCHEMA, jsonSchema);
        return new DiagnosisPrompt(systemPrompt, userPrompt, jsonSchema);
    }

    private String render(PromptTemplateType type, Map<String, Object> variables) {
        PromptTemplate template = load(type);
        PromptRenderResult result = renderer.render(new PromptRenderRequest(type, template.content(), variables, properties.strictRendering()));
        return result.renderedContent();
    }

    private PromptTemplate load(PromptTemplateType type) {
        PromptTemplate template = loader.load(type);
        if (template.contentType() == PromptContentType.JSON) {
            validateJson(type, template.content());
        }
        return template;
    }

    private void validateJson(PromptTemplateType type, String content) {
        try {
            objectMapper.readTree(content);
        } catch (JsonProcessingException ex) {
            throw new PromptTemplateLoadException(type, type.defaultPath(), ex);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new PromptRenderException(null, "Unable to serialize prompt context", ex);
        }
    }

    private String evidenceBullets(List<EvidenceItem> items) {
        if (items == null || items.isEmpty()) {
            return "- No evidence items extracted.";
        }
        return items.stream()
                .map(item -> "- " + item.title() + ": " + item.summary())
                .collect(Collectors.joining("\n"));
    }

    private String timelineBullets(IncidentTimeline timeline) {
        if (timeline == null || timeline.events().isEmpty()) {
            return "- No high-risk timeline events extracted.";
        }
        return timeline.events().stream()
                .map(this::timelineLine)
                .collect(Collectors.joining("\n"));
    }

    private String timelineLine(TimelineEvent event) {
        return "- " + event.time() + " " + event.severity() + " " + event.summary();
    }

    private String suspectedBullets(List<SuspectedCodeArea> areas) {
        if (areas == null || areas.isEmpty()) {
            return "- No suspected business code area extracted from logs.";
        }
        return areas.stream()
                .map(area -> "- " + String.join(", ", area.suspectedClasses()) + ": " + area.reason())
                .collect(Collectors.joining("\n"));
    }
}
