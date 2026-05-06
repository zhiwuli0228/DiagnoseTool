/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.pattern;

import com.geek.threaddoctor.common.ConfidenceLevel;
import com.geek.threaddoctor.evidence.EvidenceType;
import org.springframework.stereotype.Component;

@Component
// 识别多个线程等待同一把锁的情况，优先用于定位 synchronized/锁竞争导致的卡顿。
/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class LockContentionDetector implements FaultPatternDetector {
    /**
     * 获取故障模式类型。
     *
     * @return 故障模式类型
     */
    @Override
    public FaultPatternType type() {
        return FaultPatternType.LOCK_CONTENTION;
    }

    /**
     * 执行故障模式检测。
     *
     * @param context 诊断上下文
     * @return 检测结果
     */
    @Override
    public DetectionResult detect(DiagnosisContext context) {
        // 锁竞争证据完全来自 jstack 聚合结果，因此命中时置信度较高。
        boolean hit = context.jstackAnalysis().map(r -> !r.lockContentions().isEmpty()).orElse(false);
        if (!hit) {
            return DetectionResult.noMatch(type());
        }
        return new DetectionResult(type(), true, ConfidenceLevel.HIGH, "Multiple threads wait for the same lock",
                context.evidences().stream().filter(e -> e.getType() == EvidenceType.JSTACK).map(e -> e.getId()).toList());
    }
}
