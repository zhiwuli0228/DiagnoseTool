/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.diagnosis;

import com.geek.threaddoctor.common.RiskLevel;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record RecommendedAction(String title, RiskLevel riskLevel, boolean needApproval, String verification) {
}
