/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.metrics;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record JvmMetrics(long heapUsed, long heapMax, long oldGenUsed, long oldGenMax, int liveThreads, long youngGcCount, long fullGcCount, double processCpuLoad) {
}
