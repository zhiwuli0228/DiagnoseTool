/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.pattern;

import com.geek.threaddoctor.common.ConfidenceLevel;
import com.geek.threaddoctor.evidence.EvidenceType;
import org.springframework.stereotype.Component;

@Component
// 识别 Kafka 消费滞后和消费线程阻塞，适合定位消息堆积类故障。
/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class KafkaLagDetector implements FaultPatternDetector {
    /**
     * 获取故障模式类型。
     *
     * @return 故障模式类型
     */
    @Override
    public FaultPatternType type() {
        return FaultPatternType.KAFKA_LAG_INCREASED;
    }

    /**
     * 执行故障模式检测。
     *
     * @param context 诊断上下文
     * @return 检测结果
     */
    @Override
    public DetectionResult detect(DiagnosisContext context) {
        // 1000 条 lag 是 MVP 的告警阈值，真实环境应按 topic 速率和 SLA 调整。
        boolean lagHit = context.kafkaMetrics().map(m -> m.totalLag() > 1000).orElse(false);
        boolean threadHit = context.jstackAnalysis()
                .map(r -> r.suspiciousThreads().stream().anyMatch(t -> t.reason().equals("KAFKA_BLOCKED")))
                .orElse(false);
        if (lagHit || threadHit) {
            return new DetectionResult(type(), true, lagHit && threadHit ? ConfidenceLevel.HIGH : ConfidenceLevel.MEDIUM_HIGH,
                    "Kafka consumer lag or consumer blocking detected",
                    context.evidences().stream().filter(e -> e.getType() == EvidenceType.KAFKA_METRICS || e.getType() == EvidenceType.JSTACK).map(e -> e.getId()).toList());
        }
        return DetectionResult.noMatch(type());
    }
}
