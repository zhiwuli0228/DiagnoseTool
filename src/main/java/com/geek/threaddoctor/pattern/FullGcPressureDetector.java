package com.geek.threaddoctor.pattern;

import com.geek.threaddoctor.common.ConfidenceLevel;
import com.geek.threaddoctor.evidence.EvidenceType;
import org.springframework.stereotype.Component;

@Component
// 识别 Full GC 或老年代压力，适合定位 JVM 内存导致的吞吐下降和响应变慢。
public class FullGcPressureDetector implements FaultPatternDetector {
    @Override
    public FaultPatternType type() {
        return FaultPatternType.FULL_GC_PRESSURE;
    }

    @Override
    public DetectionResult detect(DiagnosisContext context) {
        // 只要发生 Full GC，或老年代使用率达到 85%，就认为存在明显内存压力。
        boolean metricsHit = context.jvmMetrics()
                .map(m -> m.fullGcCount() > 0 || (m.oldGenMax() > 0 && m.oldGenUsed() * 100 / m.oldGenMax() >= 85))
                .orElse(false);
        boolean logHit = context.evidences().stream()
                // allocation failure 是常见 GC 日志线索，可在缺少完整指标时提高召回率。
                .anyMatch(e -> e.getContent() != null && e.getContent().toLowerCase().contains("allocation failure"));
        if (metricsHit || logHit) {
            return new DetectionResult(type(), true, metricsHit ? ConfidenceLevel.HIGH : ConfidenceLevel.MEDIUM,
                    "JVM memory or Full GC pressure detected",
                    context.evidences().stream().filter(e -> e.getType() == EvidenceType.JVM_METRICS || e.getType() == EvidenceType.LOG_SNIPPET).map(e -> e.getId()).toList());
        }
        return DetectionResult.noMatch(type());
    }
}
