package com.geek.threaddoctor.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import com.geek.threaddoctor.common.ConfidenceLevel;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.loganalysis.EvidencePack;
import com.geek.threaddoctor.loganalysis.IncidentTimeline;
import java.util.List;
import org.junit.jupiter.api.Test;

class PromptAssemblyServiceTest {
    private final PromptAssemblyService service = PromptTestFactory.assemblyService();

    @Test
    void assemblesDiagnosisPromptWithSchema() {
        EvidencePack pack = pack();

        DiagnosisPrompt prompt = service.buildDiagnosisPrompt(pack, new DiagnosisRequest("Find root cause", "INC-1"));

        assertThat(prompt.systemPrompt()).contains("Thread Doctor");
        assertThat(prompt.userPrompt()).contains("Find root cause").contains("Evidence Pack");
        assertThat(prompt.jsonSchema()).contains("\"summary\"").contains("\"confidence\"");
    }

    @Test
    void rendersGeneratedArtifactPrompts() {
        EvidencePack pack = pack();

        assertThat(service.buildCodexTaskPrompt(pack)).contains("JUnit 5 with Mockito").contains("Incident Summary");
        assertThat(service.buildOpenSpecChangeDraftPrompt(pack)).contains("OpenSpec Change Draft").contains("Acceptance Criteria");
        assertThat(service.buildIncidentReviewPrompt(pack, new DiagnosisReport("RPT-1", "LOG-1", "Redis timeout", ConfidenceLevel.HIGH, "{}")))
                .contains("Incident Result Document")
                .contains("Redis timeout");
    }

    private EvidencePack pack() {
        return new EvidencePack(
                "LOG-1",
                "source summary",
                List.of(),
                "Redis timeout in payment service",
                List.of(),
                new IncidentTimeline("LOG-1", List.of()),
                List.of(),
                List.of(),
                List.of("What changed?"),
                List.of("Run tests"),
                List.of("Logs only"));
    }
}
