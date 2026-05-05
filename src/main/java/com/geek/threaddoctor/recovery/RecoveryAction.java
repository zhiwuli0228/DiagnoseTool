package com.geek.threaddoctor.recovery;

import com.geek.threaddoctor.common.ExecutionMode;
import com.geek.threaddoctor.common.RiskLevel;
import java.time.LocalDateTime;

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

    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public boolean isNeedApproval() { return needApproval; }
    public String getVerification() { return verification; }
    public ExecutionMode getExecutionMode() { return executionMode; }
    public String getExecutionResult() { return executionResult; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void simulate(String result) {
        this.executionMode = ExecutionMode.SIMULATED;
        this.executionResult = result;
    }
}
