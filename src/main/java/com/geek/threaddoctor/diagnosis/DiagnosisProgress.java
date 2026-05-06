/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.diagnosis;

import java.time.LocalDateTime;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class DiagnosisProgress {
    private final String sessionId;
    private final DiagnosisProgressStatus status;
    private final int percent;
    private final DiagnosisProgressStep step;
    private final String message;
    private final LocalDateTime startedAt;
    private final LocalDateTime updatedAt;
    private final String errorMessage;

    /**
     * 执行业务操作。
     *
     * @param sessionId 会话标识
     * @param status 目标状态
     * @param percent 业务参数
     * @param step 业务参数
     * @param message 消息内容
     * @param startedAt 业务参数
     * @param updatedAt 业务参数
     * @param errorMessage 业务参数
     */
    public DiagnosisProgress(String sessionId, DiagnosisProgressStatus status, int percent,
                             DiagnosisProgressStep step, String message, LocalDateTime startedAt,
                             LocalDateTime updatedAt, String errorMessage) {
        this.sessionId = sessionId;
        this.status = status;
        this.percent = Math.max(0, Math.min(100, percent));
        this.step = step;
        this.message = message;
        this.startedAt = startedAt;
        this.updatedAt = updatedAt;
        this.errorMessage = errorMessage;
    }

    /**
     * 创建初始诊断进度。
     *
     * @param sessionId 会话标识
     * @return 初始诊断进度
     */
    public static DiagnosisProgress notStarted(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        return new DiagnosisProgress(sessionId, DiagnosisProgressStatus.NOT_STARTED, 0,
                DiagnosisProgressStep.PENDING, "Diagnosis has not started.", now, now, null);
    }

    /**
     * 推进诊断进度。
     *
     * @param nextStep 业务参数
     * @param nextPercent 业务参数
     * @param nextMessage 业务参数
     * @return 推进后的诊断进度
     */
    public DiagnosisProgress advance(DiagnosisProgressStep nextStep, int nextPercent, String nextMessage) {
        return new DiagnosisProgress(sessionId, DiagnosisProgressStatus.RUNNING, Math.max(percent, nextPercent),
                nextStep, nextMessage, startedAt, LocalDateTime.now(), null);
    }

    /**
     * 完成当前操作。
     *
     * @param completeMessage 业务参数
     * @return 操作结果
     */
    public DiagnosisProgress complete(String completeMessage) {
        return new DiagnosisProgress(sessionId, DiagnosisProgressStatus.COMPLETED, 100,
                DiagnosisProgressStep.COMPLETED, completeMessage, startedAt, LocalDateTime.now(), null);
    }

    /**
     * 标记诊断失败。
     *
     * @param failedMessage 业务参数
     * @param errorMessage 业务参数
     * @return 失败状态的诊断进度
     */
    public DiagnosisProgress fail(String failedMessage, String errorMessage) {
        return new DiagnosisProgress(sessionId, DiagnosisProgressStatus.FAILED, percent,
                DiagnosisProgressStep.FAILED, failedMessage, startedAt, LocalDateTime.now(), errorMessage);
    }

    /**
     * 获取业务字段。
     *
     * @return 会话标识
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 获取业务字段。
     *
     * @return 状态
     */
    public DiagnosisProgressStatus getStatus() {
        return status;
    }

    /**
     * 获取业务字段。
     *
     * @return 完成百分比
     */
    public int getPercent() {
        return percent;
    }

    /**
     * 获取业务字段。
     *
     * @return 进度步骤
     */
    public DiagnosisProgressStep getStep() {
        return step;
    }

    /**
     * 获取业务字段。
     *
     * @return 进度消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取业务字段。
     *
     * @return 开始时间
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * 获取业务字段。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 获取业务字段。
     *
     * @return 错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
