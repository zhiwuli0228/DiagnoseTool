package com.geek.threaddoctor.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.geek.threaddoctor.common.ConfidenceLevel;
import com.geek.threaddoctor.common.ExecutionMode;
import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.common.RiskLevel;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.diagnosis.DiagnosisReportService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecoveryActionServiceTest {
    @Mock
    RecoveryActionRepository repository;
    @Mock
    DiagnosisReportService diagnosisReportService;

    @Test
    void generatesRiskClassifiedRedisActions() {
        when(diagnosisReportService.latest("INC-1")).thenReturn(new DiagnosisReport("RPT-1", "INC-1", "s", ConfidenceLevel.HIGH, "REDIS_POOL_EXHAUSTED"));
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RecoveryActionService service = new RecoveryActionService(repository, diagnosisReportService);

        List<RecoveryAction> actions = service.generate("INC-1");

        assertThat(actions).hasSize(3);
        assertThat(actions).extracting(RecoveryAction::getRiskLevel).contains(RiskLevel.LOW_RISK, RiskLevel.MEDIUM_RISK, RiskLevel.HIGH_RISK);
        assertThat(actions.stream().filter(a -> a.getRiskLevel() == RiskLevel.HIGH_RISK).findFirst()).get().extracting(RecoveryAction::isNeedApproval).isEqualTo(true);
    }

    @Test
    void simulatedExecutionNeverPerformsRealAction() {
        RecoveryAction action = new RecoveryAction("ACT-1", "INC-1", "restart", "no real restart", RiskLevel.HIGH_RISK, true, "verify");
        when(repository.findById("ACT-1")).thenReturn(Optional.of(action));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RecoveryActionService service = new RecoveryActionService(repository, diagnosisReportService);

        RecoveryAction executed = service.simulate("INC-1", "ACT-1");

        assertThat(executed.getExecutionMode()).isEqualTo(ExecutionMode.SIMULATED);
        assertThat(executed.getExecutionResult()).contains("no production operation");
    }

    @Test
    void reportsCacheMissForMissingRecoveryAction() {
        when(repository.findById("ACT-404")).thenReturn(Optional.empty());
        RecoveryActionService service = new RecoveryActionService(repository, diagnosisReportService);

        assertThatThrownBy(() -> service.simulate("INC-1", "ACT-404"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recovery action not found");
    }
}
