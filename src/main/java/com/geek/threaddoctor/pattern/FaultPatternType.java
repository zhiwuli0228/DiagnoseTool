/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.pattern;

/**
 * 定义固定的业务取值。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public enum FaultPatternType {
    JAVA_DEADLOCK,
    LOCK_CONTENTION,
    THREAD_POOL_EXHAUSTED,
    REDIS_POOL_EXHAUSTED,
    REDIS_IO_BLOCKED,
    KAFKA_LAG_INCREASED,
    DB_POOL_EXHAUSTED,
    FULL_GC_PRESSURE,
    SCHEDULE_TASK_STUCK,
    CACHE_INCONSISTENCY
}
