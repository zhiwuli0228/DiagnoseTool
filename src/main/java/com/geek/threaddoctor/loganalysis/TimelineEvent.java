/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record TimelineEvent(
        LocalDateTime time,
        String eventType,
        String severity,
        String summary,
        String sourceFile,
        String threadName,
        String traceId,
        String relatedClusterId,
        String evidenceEventId) {
}
