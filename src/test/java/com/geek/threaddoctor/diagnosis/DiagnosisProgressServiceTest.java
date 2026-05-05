package com.geek.threaddoctor.diagnosis;

import static org.assertj.core.api.Assertions.assertThat;

import com.geek.threaddoctor.common.cache.CacheProperties;
import org.junit.jupiter.api.Test;

class DiagnosisProgressServiceTest {
    @Test
    void savesAndReadsRunningProgress() {
        DiagnosisProgressService service = newService();

        service.reset("INC-1");
        DiagnosisProgress progress = service.advance("INC-1", DiagnosisProgressStep.BUILDING_CONTEXT, 25, "Building context.");

        assertThat(progress.getStatus()).isEqualTo(DiagnosisProgressStatus.RUNNING);
        assertThat(progress.getPercent()).isEqualTo(25);
        assertThat(service.current("INC-1").getStep()).isEqualTo(DiagnosisProgressStep.BUILDING_CONTEXT);
    }

    @Test
    void keepsProgressPercentMonotonic() {
        DiagnosisProgressService service = newService();

        service.reset("INC-1");
        service.advance("INC-1", DiagnosisProgressStep.GENERATING_REPORT, 70, "Generating report.");
        DiagnosisProgress progress = service.advance("INC-1", DiagnosisProgressStep.DETECTING_PATTERNS, 45, "Detecting patterns.");

        assertThat(progress.getPercent()).isEqualTo(70);
    }

    @Test
    void completesProgressAtOneHundredPercent() {
        DiagnosisProgressService service = newService();

        service.reset("INC-1");
        DiagnosisProgress progress = service.complete("INC-1");

        assertThat(progress.getStatus()).isEqualTo(DiagnosisProgressStatus.COMPLETED);
        assertThat(progress.getPercent()).isEqualTo(100);
    }

    @Test
    void recordsFailedProgressWithErrorMessage() {
        DiagnosisProgressService service = newService();

        service.reset("INC-1");
        DiagnosisProgress progress = service.fail("INC-1", new IllegalArgumentException("bad json"));

        assertThat(progress.getStatus()).isEqualTo(DiagnosisProgressStatus.FAILED);
        assertThat(progress.getErrorMessage()).isEqualTo("bad json");
    }

    @Test
    void returnsNotStartedForCacheMiss() {
        DiagnosisProgress progress = newService().current("INC-404");

        assertThat(progress.getStatus()).isEqualTo(DiagnosisProgressStatus.NOT_STARTED);
        assertThat(progress.getPercent()).isZero();
    }

    private DiagnosisProgressService newService() {
        return new DiagnosisProgressService(new InMemoryDiagnosisProgressRepository(new CacheProperties(100, 60)));
    }
}
