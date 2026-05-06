package com.geek.threaddoctor.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.geek.threaddoctor.common.ConfidenceLevel;
import com.geek.threaddoctor.common.SeverityLevel;
import com.geek.threaddoctor.diagnosis.DiagnosisProgress;
import com.geek.threaddoctor.diagnosis.DiagnosisProgressService;
import com.geek.threaddoctor.diagnosis.DiagnosisProgressStatus;
import com.geek.threaddoctor.diagnosis.DiagnosisProgressStep;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.diagnosis.DiagnosisReportService;
import com.geek.threaddoctor.evidence.Evidence;
import com.geek.threaddoctor.evidence.EvidenceService;
import com.geek.threaddoctor.evidence.EvidenceType;
import com.geek.threaddoctor.incident.IncidentSession;
import com.geek.threaddoctor.incident.IncidentSessionService;
import com.geek.threaddoctor.memory.IncidentCard;
import com.geek.threaddoctor.memory.IncidentCardService;
import com.geek.threaddoctor.metrics.MetricsSnapshot;
import com.geek.threaddoctor.metrics.MetricsSnapshotService;
import com.geek.threaddoctor.common.RiskLevel;
import com.geek.threaddoctor.common.ApiExceptionHandler;
import com.geek.threaddoctor.recovery.RecoveryAction;
import com.geek.threaddoctor.recovery.RecoveryActionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiContractTest {
    IncidentSessionService incidentSessionService = Mockito.mock(IncidentSessionService.class);
    EvidenceService evidenceService = Mockito.mock(EvidenceService.class);
    MetricsSnapshotService metricsSnapshotService = Mockito.mock(MetricsSnapshotService.class);
    DiagnosisReportService diagnosisReportService = Mockito.mock(DiagnosisReportService.class);
    DiagnosisProgressService diagnosisProgressService = Mockito.mock(DiagnosisProgressService.class);
    RecoveryActionService recoveryActionService = Mockito.mock(RecoveryActionService.class);
    IncidentCardService incidentCardService = Mockito.mock(IncidentCardService.class);
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new IncidentController(incidentSessionService, evidenceService),
                new MetricsController(metricsSnapshotService),
                new DiagnosisController(diagnosisReportService, diagnosisProgressService),
                new RecoveryController(recoveryActionService),
                new IncidentCardController(incidentCardService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void supportsMainIncidentApiFlow() throws Exception {
        IncidentSession session = new IncidentSession("INC-1", "Redis timeout", "demo", SeverityLevel.HIGH);
        Evidence evidence = new Evidence("EVD-1", "INC-1", EvidenceType.LOG_SNIPPET, "manual", "error", "{}");
        MetricsSnapshot metrics = new MetricsSnapshot("MTR-1", "INC-1", "{}", "{\"active\":10}", "{}", "{}");
        DiagnosisReport report = new DiagnosisReport("RPT-1", "INC-1", "redis", ConfidenceLevel.HIGH, "REDIS_POOL_EXHAUSTED");
        DiagnosisProgress runningProgress = new DiagnosisProgress("INC-1", DiagnosisProgressStatus.RUNNING, 70,
                DiagnosisProgressStep.GENERATING_REPORT, "Generating diagnosis report.", LocalDateTime.now(), LocalDateTime.now(), null);
        RecoveryAction action = new RecoveryAction("ACT-1", "INC-1", "Check Redis", "Inspect Redis", RiskLevel.LOW_RISK, false, "Watch timeout count");
        IncidentCard card = new IncidentCard("CARD-1", "INC-1", "# Incident Card", "REDIS_POOL_EXHAUSTED");
        when(incidentSessionService.create(any(), any(), any())).thenReturn(session);
        when(incidentSessionService.getRequired("INC-1")).thenReturn(session);
        when(evidenceService.listBySession("INC-1")).thenReturn(List.of(evidence));
        when(evidenceService.upload(any(), any(), any(), any(), any())).thenReturn(evidence);
        when(metricsSnapshotService.save(any(), any(), any(), any(), any())).thenReturn(metrics);
        when(diagnosisReportService.diagnose("INC-1")).thenReturn(report);
        when(diagnosisProgressService.current("INC-1")).thenReturn(runningProgress);
        when(recoveryActionService.generate("INC-1")).thenReturn(List.of(action));
        when(recoveryActionService.simulate("INC-1", "ACT-1")).thenReturn(action);
        when(incidentCardService.generate("INC-1")).thenReturn(card);

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Redis timeout\",\"description\":\"demo\",\"severity\":\"HIGH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("INC-1"));

        mockMvc.perform(post("/api/incidents/INC-1/evidences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"LOG_SNIPPET\",\"source\":\"manual\",\"content\":\"error\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("EVD-1"));

        mockMvc.perform(get("/api/incidents/INC-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value("INC-1"))
                .andExpect(jsonPath("$.evidences[0].id").value("EVD-1"));

        mockMvc.perform(post("/api/incidents/INC-1/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"redisMetricsJson\":\"{\\\"active\\\":10}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("MTR-1"));

        mockMvc.perform(post("/api/incidents/INC-1/diagnose"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("RPT-1"));

        mockMvc.perform(get("/api/incidents/INC-1/diagnosis-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.percent").value(70))
                .andExpect(jsonPath("$.step").value("GENERATING_REPORT"));

        mockMvc.perform(post("/api/incidents/INC-1/recovery-actions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ACT-1"));

        mockMvc.perform(post("/api/incidents/INC-1/recovery-actions/ACT-1/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ACT-1"));

        mockMvc.perform(post("/api/incidents/INC-1/incident-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CARD-1"));
    }

    @Test
    void returnsNotStartedProgressWhenProgressIsMissing() throws Exception {
        DiagnosisProgress notStarted = DiagnosisProgress.notStarted("INC-404");
        when(diagnosisProgressService.current("INC-404")).thenReturn(notStarted);

        mockMvc.perform(get("/api/incidents/INC-404/diagnosis-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.percent").value(0))
                .andExpect(jsonPath("$.step").value("PENDING"));
    }

    @Test
    void returnsCompletedProgress() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        DiagnosisProgress completed = new DiagnosisProgress("INC-1", DiagnosisProgressStatus.COMPLETED, 100,
                DiagnosisProgressStep.COMPLETED, "Diagnosis completed.", now, now, null);
        when(diagnosisProgressService.current("INC-1")).thenReturn(completed);

        mockMvc.perform(get("/api/incidents/INC-1/diagnosis-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.percent").value(100))
                .andExpect(jsonPath("$.step").value("COMPLETED"));
    }

    @Test
    void rejectsMalformedFrontendInputs() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "x".repeat(121) + "\",\"severity\":\"HIGH\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("title")));

        mockMvc.perform(post("/api/incidents/INC-1/evidences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"NOT_A_TYPE\",\"content\":\"error\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/incidents/INC-1/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"redisMetricsJson\":\"" + "x".repeat(100001) + "\"}"))
                .andExpect(status().isBadRequest());
    }
}
