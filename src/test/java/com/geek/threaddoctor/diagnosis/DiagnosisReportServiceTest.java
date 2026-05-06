package com.geek.threaddoctor.diagnosis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geek.threaddoctor.common.ConfidenceLevel;
import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.common.SeverityLevel;
import com.geek.threaddoctor.evidence.Evidence;
import com.geek.threaddoctor.evidence.EvidenceType;
import com.geek.threaddoctor.incident.IncidentSession;
import com.geek.threaddoctor.incident.IncidentSessionService;
import com.geek.threaddoctor.llm.LlmClient;
import com.geek.threaddoctor.llm.LlmRequest;
import com.geek.threaddoctor.llm.LlmResponse;
import com.geek.threaddoctor.pattern.DetectionResult;
import com.geek.threaddoctor.pattern.DiagnosisContext;
import com.geek.threaddoctor.pattern.FaultPatternDetectionService;
import com.geek.threaddoctor.pattern.FaultPatternType;
import com.geek.threaddoctor.prompt.PromptTestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiagnosisReportServiceTest {
    @Mock
    DiagnosisContextBuilder contextBuilder;
    @Mock
    FaultPatternDetectionService detectionService;
    @Mock
    DiagnosisReportRepository repository;
    @Mock
    IncidentSessionService incidentSessionService;
    @Mock
    LlmClient llmClient;
    @Mock
    DiagnosisProgressService progressService;

    @Test
    void persistsValidatedReport() {
        DiagnosisContext context = new DiagnosisContext(new IncidentSession("INC-1", "t", "d", SeverityLevel.HIGH),
                List.of(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), List.of());
        when(contextBuilder.build("INC-1")).thenReturn(context);
        when(detectionService.detect(context)).thenReturn(List.of(new DetectionResult(FaultPatternType.REDIS_POOL_EXHAUSTED, true, ConfidenceLevel.HIGH, "redis", List.of("EVD-1"))));
        when(llmClient.complete(any())).thenReturn(new LlmResponse("{\"summary\":\"redis pool exhausted\",\"confidence\":\"HIGH\",\"localizationStatus\":\"LOCALIZED\"}", "mock", 1, 1));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        DiagnosisReportService service = newService();

        DiagnosisReport report = service.diagnose("INC-1");

        assertThat(report.getSummary()).isEqualTo("redis pool exhausted");
        assertThat(report.getConfidence()).isEqualTo(ConfidenceLevel.HIGH);
        assertThat(report.getLocalizationStatus()).isEqualTo(DiagnosisLocalizationStatus.LOCALIZED);
        assertThat(report.getReportJson()).contains("REDIS_POOL_EXHAUSTED");
        verify(progressService).reset("INC-1");
        verify(progressService).advance(eq("INC-1"), eq(DiagnosisProgressStep.BUILDING_CONTEXT), eq(25), any());
        verify(progressService).advance(eq("INC-1"), eq(DiagnosisProgressStep.DETECTING_PATTERNS), eq(45), any());
        verify(progressService).advance(eq("INC-1"), eq(DiagnosisProgressStep.GENERATING_REPORT), eq(70), any());
        verify(progressService).advance(eq("INC-1"), eq(DiagnosisProgressStep.VALIDATING_REPORT), eq(90), any());
        verify(progressService).advance(eq("INC-1"), eq(DiagnosisProgressStep.PERSISTING_REPORT), eq(95), any());
        verify(progressService).complete("INC-1");
    }

    @Test
    void sendsSubmittedLogEvidenceToLlmAndReportJson() {
        Evidence log = new Evidence("EVD-LOG-1", "INC-1", EvidenceType.LOG_SNIPPET, "conversation",
                "java.net.SocketTimeoutException: Redis read timed out", "{}");
        DiagnosisContext context = new DiagnosisContext(new IncidentSession("INC-1", "Redis timeout", "latency increased", SeverityLevel.HIGH),
                List.of(log), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), List.of("jstack evidence is missing"));
        when(contextBuilder.build("INC-1")).thenReturn(context);
        when(detectionService.detect(context)).thenReturn(List.of());
        when(llmClient.complete(any())).thenReturn(new LlmResponse("{\"summary\":\"redis timeout from log\",\"confidence\":\"MEDIUM_HIGH\"}", "mock", 1, 1));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        DiagnosisReportService service = newService();

        DiagnosisReport report = service.diagnose("INC-1");

        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(llmClient).complete(captor.capture());
        Map<String, Object> variables = captor.getValue().variables();
        assertThat(variables).containsKeys("incident", "evidences", "detections", "missingInformation");
        assertThat(variables.get("evidences").toString())
                .contains("EVD-LOG-1")
                .contains("LOG_SNIPPET")
                .contains("SocketTimeoutException");
        assertThat(captor.getValue().prompt()).contains("Evidence Pack");
        assertThat(report.getReportJson())
                .contains("evidencesUsed")
                .contains("EVD-LOG-1")
                .contains("SocketTimeoutException");
    }

    @Test
    void fallsBackToUnresolvedForInvalidLlmJson() {
        DiagnosisContext context = new DiagnosisContext(new IncidentSession("INC-1", "t", "d", SeverityLevel.HIGH),
                List.of(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), List.of());
        when(contextBuilder.build("INC-1")).thenReturn(context);
        when(detectionService.detect(context)).thenReturn(List.of());
        when(llmClient.complete(any())).thenReturn(new LlmResponse("not-json", "mock", 1, 1));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        DiagnosisReportService service = newService();

        DiagnosisReport report = service.diagnose("INC-1");

        assertThat(report.getLocalizationStatus()).isEqualTo(DiagnosisLocalizationStatus.UNRESOLVED);
        assertThat(report.getUnresolvedReasons()).anyMatch(reason -> reason.contains("valid diagnosis JSON"));
        assertThat(report.getCodebasePrompt()).isNotNull();
        assertThat(report.getCodebasePrompt().markdown()).contains("Continue Diagnosis With Codebase Context");
        verify(progressService).complete("INC-1");
    }

    @Test
    void acceptsJsonWrappedInMarkdownFence() {
        DiagnosisContext context = new DiagnosisContext(new IncidentSession("INC-1", "t", "d", SeverityLevel.HIGH),
                List.of(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), List.of());
        when(contextBuilder.build("INC-1")).thenReturn(context);
        when(detectionService.detect(context)).thenReturn(List.of());
        when(llmClient.complete(any())).thenReturn(new LlmResponse("```json\n{\"summary\":\"wrapped\",\"confidence\":\"MEDIUM\",\"localizationStatus\":\"UNRESOLVED\",\"unresolvedReasons\":[\"Need codebase check\"]}\n```", "mock", 1, 1));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        DiagnosisReportService service = newService();

        DiagnosisReport report = service.diagnose("INC-1");

        assertThat(report.getSummary()).isEqualTo("wrapped");
        assertThat(report.getConfidence()).isEqualTo(ConfidenceLevel.MEDIUM);
        assertThat(report.getLocalizationStatus()).isEqualTo(DiagnosisLocalizationStatus.UNRESOLVED);
    }

    @Test
    void requestsMoreEvidenceAndGeneratesMaskedCodebasePrompt() {
        Evidence log = new Evidence("EVD-LOG-1", "INC-1", EvidenceType.LOG_SNIPPET, "conversation",
                "timeout after apiKey=sk-secret123456789 token=abcd1234", "{}");
        DiagnosisContext context = new DiagnosisContext(new IncidentSession("INC-1", "Redis timeout", "latency increased", SeverityLevel.HIGH),
                List.of(log), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), List.of("jstack evidence is missing"));
        when(contextBuilder.build("INC-1")).thenReturn(context);
        when(detectionService.detect(context)).thenReturn(List.of());
        when(llmClient.complete(any())).thenReturn(new LlmResponse("""
                {
                  "summary": "Need thread evidence",
                  "confidence": "LOW",
                  "localizationStatus": "NEEDS_MORE_EVIDENCE",
                  "unresolvedReasons": ["Only timeout logs are available"],
                  "followUpEvidenceRequests": [{
                    "title": "Capture jstack",
                    "reason": "Need blocked threads",
                    "expectedFormat": "jstack text",
                    "guidance": "Submit jstack during timeout"
                  }]
                }
                """, "mock", 1, 1));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        DiagnosisReportService service = newService();

        DiagnosisReport report = service.diagnose("INC-1");

        assertThat(report.getLocalizationStatus()).isEqualTo(DiagnosisLocalizationStatus.NEEDS_MORE_EVIDENCE);
        assertThat(report.getFollowUpEvidenceRequests()).extracting(FollowUpEvidenceRequest::title).contains("Capture jstack");
        assertThat(report.getCodebasePrompt().markdown()).contains("Need thread evidence").contains("Only timeout logs are available");
        assertThat(report.getCodebasePrompt().markdown()).doesNotContain("sk-secret123456789").doesNotContain("abcd1234");
    }

    @Test
    void readsLatestCachedReport() {
        DiagnosisReport cached = new DiagnosisReport("RPT-1", "INC-1", "cached", ConfidenceLevel.HIGH, "{}");
        when(repository.findTopBySessionIdOrderByGeneratedAtDesc("INC-1")).thenReturn(Optional.of(cached));
        DiagnosisReportService service = newService();

        DiagnosisReport report = service.latest("INC-1");

        assertThat(report.getId()).isEqualTo("RPT-1");
    }

    @Test
    void reportsCacheMissForMissingReport() {
        when(repository.findTopBySessionIdOrderByGeneratedAtDesc("INC-404")).thenReturn(Optional.empty());
        DiagnosisReportService service = newService();

        assertThatThrownBy(() -> service.latest("INC-404"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Diagnosis report not found");
    }

    private DiagnosisReportService newService() {
        return new DiagnosisReportService(contextBuilder, detectionService, repository, incidentSessionService,
                llmClient, new ObjectMapper(), progressService, PromptTestFactory.assemblyService());
    }
}
