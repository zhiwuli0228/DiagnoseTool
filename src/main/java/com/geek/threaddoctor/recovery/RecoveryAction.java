/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.recovery;

import com.geek.threaddoctor.common.ExecutionMode;
import com.geek.threaddoctor.common.RiskLevel;
import java.time.LocalDateTime;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class RecoveryAction {
    private String id;
    private String sessionId;
    private String title;
    private String description;
    private RiskLevel riskLevel;
    private boolean needApproval;
    private String verification;
    private ExecutionMode executionMode;
    private String executionResult;
    private LocalDateTime createdAt;

    protected RecoveryAction() {
    }

    /**
     * 执行业务操作。
     *
     * @param id 记录标识
     * @param sessionId 会话标识
     * @param title 标题
     * @param description 描述信息
     * @param riskLevel 业务参数
     * @param needApproval 业务参数
     * @param verification 业务参数
     */
    public RecoveryAction(String id, String sessionId, String title, String description, RiskLevel riskLevel, boolean needApproval, String verification) {
        this.id = id;
        this.sessionId = sessionId;
        this.title = title;
        this.description = description;
        this.riskLevel = riskLevel;
        this.needApproval = needApproval;
        this.verification = verification;
        this.createdAt = LocalDateTime.now();
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
     * @return 标题
     */
    public String getTitle() { return title; }
    /**
     * 获取业务字段。
     *
     * @return 描述信息
     */
    public String getDescription() { return description; }
    /**
     * 获取业务字段。
     *
     * @return 风险级别
     */
    public RiskLevel getRiskLevel() { return riskLevel; }
    /**
     * 判断业务条件是否成立。
     *
     * @return 是否需要审批
     */
    public boolean isNeedApproval() { return needApproval; }
    /**
     * 获取业务字段。
     *
     * @return 验证指引
     */
    public String getVerification() { return verification; }
    /**
     * 获取业务字段。
     *
     * @return 执行模式
     */
    public ExecutionMode getExecutionMode() { return executionMode; }
    /**
     * 获取业务字段。
     *
     * @return 执行结果
     */
    public String getExecutionResult() { return executionResult; }
    /**
     * 获取业务字段。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * 模拟执行恢复动作。
     *
     * @param result 执行结果
     */
    public void simulate(String result) {
        this.executionMode = ExecutionMode.SIMULATED;
        this.executionResult = result;
    }
}
