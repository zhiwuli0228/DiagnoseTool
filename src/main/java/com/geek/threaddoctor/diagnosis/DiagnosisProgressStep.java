/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.diagnosis;

/**
 * 定义固定的业务取值。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public enum DiagnosisProgressStep {
    PENDING,
    STARTED,
    BUILDING_CONTEXT,
    DETECTING_PATTERNS,
    GENERATING_REPORT,
    VALIDATING_REPORT,
    PERSISTING_REPORT,
    COMPLETED,
    FAILED
}
