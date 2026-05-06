/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.recovery;

import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.common.RiskLevel;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.diagnosis.DiagnosisReportService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Service
public class RecoveryActionService {
    private final RecoveryActionRepository repository;
    private final DiagnosisReportService diagnosisReportService;

    /**
     * 执行业务操作。
     *
     * @param repository 仓储依赖
     * @param diagnosisReportService 业务服务依赖
     */
    public RecoveryActionService(RecoveryActionRepository repository, DiagnosisReportService diagnosisReportService) {
        this.repository = repository;
        this.diagnosisReportService = diagnosisReportService;
    }

    /**
     * 生成业务内容。
     *
     * @param sessionId 会话标识
     * @return 生成的业务内容
     */
    public List<RecoveryAction> generate(String sessionId) {
        DiagnosisReport report = diagnosisReportService.latest(sessionId);
        // 恢复建议只缓存在当前会话，所有执行都保持模拟语义。
        List<RecoveryAction> actions = defaultActions(sessionId, report.getReportJson());
        return repository.saveAll(actions);
    }

    /**
     * 模拟执行恢复动作。
     *
     * @param sessionId 会话标识
     * @param actionId 动作标识
     * @return 模拟后的恢复动作
     */
    public RecoveryAction simulate(String sessionId, String actionId) {
        RecoveryAction action = repository.findById(actionId)
                .filter(a -> a.getSessionId().equals(sessionId))
                .orElseThrow(() -> new ResourceNotFoundException("Recovery action not found: " + actionId));
        action.simulate("SIMULATED only: no production operation was executed. Verify metrics and error rate manually.");
        return repository.save(action);
    }

    /**
     * 列出业务记录。
     *
     * @param sessionId 会话标识
     * @return 业务记录集合
     */
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
