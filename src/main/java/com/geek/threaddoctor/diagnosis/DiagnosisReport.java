/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.diagnosis;

import com.geek.threaddoctor.common.ConfidenceLevel;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class DiagnosisReport {
    private String id;
    private String sessionId;
    private String summary;
    private ConfidenceLevel confidence;
    private String reportJson;
    private DiagnosisLocalizationStatus localizationStatus;
    private List<String> unresolvedReasons;
    private List<FollowUpEvidenceRequest> followUpEvidenceRequests;
    private CodebaseInvestigationPrompt codebasePrompt;
    private LocalDateTime generatedAt;

    protected DiagnosisReport() {
    }

    /**
     * 执行业务操作。
     *
     * @param id 记录标识
     * @param sessionId 会话标识
     * @param summary 业务参数
     * @param confidence 业务参数
     * @param reportJson 数据内容
     */
    public DiagnosisReport(String id, String sessionId, String summary, ConfidenceLevel confidence, String reportJson) {
        this(id, sessionId, summary, confidence, reportJson, DiagnosisLocalizationStatus.LOCALIZED, List.of(), List.of(), null);
    }

    /**
     * 执行业务操作。
     *
     * @param id 记录标识
     * @param sessionId 会话标识
     * @param summary 业务参数
     * @param confidence 业务参数
     * @param reportJson 数据内容
     * @param localizationStatus 业务参数
     * @param unresolvedReasons 业务参数
     * @param followUpEvidenceRequests 业务参数
     * @param codebasePrompt 业务参数
     */
    public DiagnosisReport(String id, String sessionId, String summary, ConfidenceLevel confidence, String reportJson,
            DiagnosisLocalizationStatus localizationStatus, List<String> unresolvedReasons,
            List<FollowUpEvidenceRequest> followUpEvidenceRequests, CodebaseInvestigationPrompt codebasePrompt) {
        this.id = id;
        this.sessionId = sessionId;
        this.summary = summary;
        this.confidence = confidence;
        this.reportJson = reportJson;
        this.localizationStatus = localizationStatus == null ? DiagnosisLocalizationStatus.LOCALIZED : localizationStatus;
        this.unresolvedReasons = unresolvedReasons == null ? List.of() : List.copyOf(unresolvedReasons);
        this.followUpEvidenceRequests = followUpEvidenceRequests == null ? List.of() : List.copyOf(followUpEvidenceRequests);
        this.codebasePrompt = codebasePrompt;
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * 获取业务字段。
     *
     * @return 记录标识
     */
    public String getId() { return id; }
    /**
     * 获取业务字段。
     *
     * @return 会话标识
     */
    public String getSessionId() { return sessionId; }
    /**
     * 获取业务字段。
     *
     * @return 摘要
     */
    public String getSummary() { return summary; }
    /**
     * 获取业务字段。
     *
     * @return 置信度
     */
    public ConfidenceLevel getConfidence() { return confidence; }
    /**
     * 获取业务字段。
     *
     * @return 报告数据
     */
    public String getReportJson() { return reportJson; }
    /**
     * 获取业务字段。
     *
     * @return 定位状态
     */
    public DiagnosisLocalizationStatus getLocalizationStatus() { return localizationStatus; }
    /**
     * 获取业务字段。
     *
     * @return 未解决原因
     */
    public List<String> getUnresolvedReasons() { return unresolvedReasons == null ? List.of() : List.copyOf(unresolvedReasons); }
    /**
     * 获取业务字段。
     *
     * @return 补充证据请求
     */
    public List<FollowUpEvidenceRequest> getFollowUpEvidenceRequests() { return followUpEvidenceRequests == null ? List.of() : List.copyOf(followUpEvidenceRequests); }
    /**
     * 获取业务字段。
     *
     * @return 代码库排查提示词
     */
    public CodebaseInvestigationPrompt getCodebasePrompt() { return codebasePrompt; }
    /**
     * 获取业务字段。
     *
     * @return 生成时间
     */
    public LocalDateTime getGeneratedAt() { return generatedAt; }
}
