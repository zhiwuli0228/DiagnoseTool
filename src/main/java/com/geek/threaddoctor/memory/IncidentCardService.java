/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.memory;

import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.diagnosis.DiagnosisReportService;
import com.geek.threaddoctor.loganalysis.EvidencePack;
import com.geek.threaddoctor.loganalysis.IncidentTimeline;
import com.geek.threaddoctor.prompt.PromptAssemblyService;
import com.geek.threaddoctor.recovery.RecoveryAction;
import com.geek.threaddoctor.recovery.RecoveryActionService;
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
public class IncidentCardService {
    private final IncidentCardRepository repository;
    private final DiagnosisReportService diagnosisReportService;
    private final RecoveryActionService recoveryActionService;
    private final PromptAssemblyService promptAssemblyService;

    /**
     * 执行业务操作。
     *
     * @param repository 仓储依赖
     * @param diagnosisReportService 业务服务依赖
     * @param recoveryActionService 业务服务依赖
     * @param promptAssemblyService 业务服务依赖
     */
    public IncidentCardService(IncidentCardRepository repository, DiagnosisReportService diagnosisReportService,
            RecoveryActionService recoveryActionService, PromptAssemblyService promptAssemblyService) {
        this.repository = repository;
        this.diagnosisReportService = diagnosisReportService;
        this.recoveryActionService = recoveryActionService;
        this.promptAssemblyService = promptAssemblyService;
    }

    /**
     * 生成业务内容。
     *
     * @param sessionId 会话标识
     * @return 生成的业务内容
     */
    public IncidentCard generate(String sessionId) {
        DiagnosisReport report = diagnosisReportService.latest(sessionId);
        List<RecoveryAction> actions = recoveryActionService.list(sessionId);
        String tags = tags(report.getReportJson());
        EvidencePack pack = incidentReviewPack(sessionId, report, actions);
        String markdown = promptAssemblyService.buildIncidentReviewPrompt(pack, report);
        return repository.save(new IncidentCard("CARD-" + UUID.randomUUID(), sessionId, markdown, tags));
    }

    /**
     * 获取最新记录。
     *
     * @param sessionId 会话标识
     * @return 最新记录
     */
    public IncidentCard latest(String sessionId) {
        return repository.findTopBySessionIdOrderByCreatedAtDesc(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident result document not found for session: " + sessionId));
    }

    private String tags(String reportJson) {
        if (reportJson != null && reportJson.contains("REDIS_POOL_EXHAUSTED")) {
            return "REDIS_POOL_EXHAUSTED";
        }
        if (reportJson != null && reportJson.contains("LOCK_CONTENTION")) {
            return "LOCK_CONTENTION";
        }
        return "INSUFFICIENT_EVIDENCE";
    }

    private EvidencePack incidentReviewPack(String sessionId, DiagnosisReport report, List<RecoveryAction> actions) {
        return new EvidencePack(
                sessionId,
                "Diagnosis report and recovery actions",
                List.of(),
                report.getSummary(),
                List.of(),
                new IncidentTimeline(sessionId, List.of()),
                List.of(),
                List.of(),
                List.of("Review whether monitoring and runbooks need updates."),
                actions.stream().map(RecoveryAction::getTitle).toList(),
                List.of(report.getReportJson()));
    }
}
