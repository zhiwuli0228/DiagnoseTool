/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.incident;

/**
 * 定义固定的业务取值。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public enum IncidentStatus {
    CREATED,
    COLLECTING_EVIDENCE,
    DIAGNOSING,
    WAITING_CONFIRMATION,
    RECOVERING,
    VERIFYING,
    RESOLVED,
    FAILED
}
