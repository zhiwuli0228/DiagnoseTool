/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.evidence;

/**
 * 定义固定的业务取值。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public enum EvidenceType {
    ALERT_TEXT,
    LOG_SNIPPET,
    JSTACK,
    JVM_METRICS,
    REDIS_METRICS,
    KAFKA_METRICS,
    DB_METRICS,
    CONFIG_CHANGE,
    MANUAL_NOTE
}
