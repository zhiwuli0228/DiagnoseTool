/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record LogCluster(
        String clusterId,
        String fingerprint,
        String exceptionType,
        long count,
        LocalDateTime firstSeen,
        LocalDateTime lastSeen,
        List<String> sampleEventIds,
        List<String> sampleLogs,
        List<String> threadNames,
        List<String> loggerNames,
        List<String> suspectedClasses,
        List<String> suspectedMethods,
        String severity) {
}
