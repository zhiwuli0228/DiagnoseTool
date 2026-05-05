package com.geek.threaddoctor.pattern;

import com.geek.threaddoctor.common.ConfidenceLevel;
import com.geek.threaddoctor.evidence.EvidenceType;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
// 识别 Redis 连接池耗尽：指标直接命中优先，日志与 jstack 可作为组合证据。
public class RedisPoolExhaustedDetector implements FaultPatternDetector {
    @Override
    public FaultPatternType type() {
        return FaultPatternType.REDIS_POOL_EXHAUSTED;
    }

    @Override
    public DetectionResult detect(DiagnosisContext context) {
        // active 达到 maxActive、idle 为 0 且存在 waiters，说明连接池已无可借连接。
        boolean metricsHit = context.redisMetrics()
                .map(m -> m.maxActive() > 0 && m.active() >= m.maxActive() && m.idle() == 0 && m.waiters() > 0)
                .orElse(false);
        boolean logHit = context.evidences().stream()
                // Jedis 常见异常文案，用于在没有连接池指标时捕获用户上传的日志片段。
                .anyMatch(e -> e.getContent() != null && e.getContent().toLowerCase().contains("could not get a resource from the pool"));
        boolean jstackHit = context.jstackAnalysis()
                .map(r -> r.suspiciousThreads().stream().anyMatch(t -> t.reason().equals("REDIS_IO_BLOCKED")))
                .orElse(false);
        if (metricsHit || (logHit && jstackHit)) {
            return new DetectionResult(type(), true, metricsHit && jstackHit ? ConfidenceLevel.HIGH : ConfidenceLevel.MEDIUM_HIGH,
                    "Redis connection pool appears exhausted", evidenceIds(context, EvidenceType.REDIS_METRICS, EvidenceType.LOG_SNIPPET, EvidenceType.JSTACK));
        }
        return DetectionResult.noMatch(type());
    }

    private java.util.List<String> evidenceIds(DiagnosisContext context, EvidenceType... types) {
        // 报告中保留证据 ID，便于人工从结论反查到原始材料。
        var wanted = Stream.of(types).toList();
        return context.evidences().stream().filter(e -> wanted.contains(e.getType())).map(e -> e.getId()).toList();
    }
}
