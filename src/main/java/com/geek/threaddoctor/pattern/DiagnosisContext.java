/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.pattern;

import com.geek.threaddoctor.evidence.Evidence;
import com.geek.threaddoctor.incident.IncidentSession;
import com.geek.threaddoctor.jstack.JstackAnalysisResult;
import com.geek.threaddoctor.metrics.KafkaMetrics;
import com.geek.threaddoctor.metrics.JvmMetrics;
import com.geek.threaddoctor.metrics.RedisMetrics;
import java.util.List;
import java.util.Optional;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record DiagnosisContext(
        IncidentSession session,
        List<Evidence> evidences,
        Optional<JstackAnalysisResult> jstackAnalysis,
        Optional<RedisMetrics> redisMetrics,
        Optional<JvmMetrics> jvmMetrics,
        Optional<KafkaMetrics> kafkaMetrics,
        List<String> missingInformation) {
}
