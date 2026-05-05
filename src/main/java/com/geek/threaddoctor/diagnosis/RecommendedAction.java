package com.geek.threaddoctor.diagnosis;

import com.geek.threaddoctor.common.RiskLevel;

public record RecommendedAction(String title, RiskLevel riskLevel, boolean needApproval, String verification) {
}
