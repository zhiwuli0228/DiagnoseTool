package com.geek.threaddoctor.recovery;

import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.common.RiskLevel;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.diagnosis.DiagnosisReportService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RecoveryActionService {
    private final RecoveryActionRepository repository;
    private final DiagnosisReportService diagnosisReportService;

    public RecoveryActionService(RecoveryActionRepository repository, DiagnosisReportService diagnosisReportService) {
        this.repository = repository;
        this.diagnosisReportService = diagnosisReportService;
    }

    public List<RecoveryAction> generate(String sessionId) {
        DiagnosisReport report = diagnosisReportService.latest(sessionId);
        // 恢复建议只缓存在当前会话，所有执行都保持模拟语义。
        List<RecoveryAction> actions = defaultActions(sessionId, report.getReportJson());
        return repository.saveAll(actions);
    }

    public RecoveryAction simulate(String sessionId, String actionId) {
        RecoveryAction action = repository.findById(actionId)
                .filter(a -> a.getSessionId().equals(sessionId))
                .orElseThrow(() -> new ResourceNotFoundException("Recovery action not found: " + actionId));
        action.simulate("SIMULATED only: no production operation was executed. Verify metrics and error rate manually.");
        return repository.save(action);
    }

    public List<RecoveryAction> list(String sessionId) {
        return repository.findBySessionId(sessionId);
    }

    private List<RecoveryAction> defaultActions(String sessionId, String reportJson) {
        boolean redis = reportJson != null && reportJson.contains("REDIS_POOL_EXHAUSTED");
        if (redis) {
            return List.of(
                    action(sessionId, "Check Redis client pool", "Inspect Redis client pool usage and timeout configuration.", RiskLevel.LOW_RISK, false,
                            "Verify Redis waiters, commandTimeoutCount, and business error rate."),
                    action(sessionId, "Tune Redis pool limits", "Review maxActive and timeout settings before changing configuration.", RiskLevel.MEDIUM_RISK, true,
                            "Compare active, idle, and borrowTimeoutCount before and after the simulated plan."),
                    action(sessionId, "Prepare controlled service recovery", "Document a high-risk recovery plan. MVP must not execute production restart or traffic changes.", RiskLevel.HIGH_RISK, true,
                            "Verify metrics manually and require operator approval outside the MVP."));
        }
        return List.of(action(sessionId, "Collect more evidence", "Add jstack, logs, and metric snapshots before choosing recovery actions.", RiskLevel.LOW_RISK, false,
                "Confirm that diagnosis missing information has been supplied."));
    }

    private RecoveryAction action(String sessionId, String title, String description, RiskLevel riskLevel, boolean approval, String verification) {
        return new RecoveryAction("ACT-" + UUID.randomUUID(), sessionId, title, description, riskLevel, approval, verification);
    }
}
