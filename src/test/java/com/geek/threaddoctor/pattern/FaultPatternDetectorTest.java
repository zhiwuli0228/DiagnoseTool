package com.geek.threaddoctor.pattern;

import static org.assertj.core.api.Assertions.assertThat;

import com.geek.threaddoctor.common.SeverityLevel;
import com.geek.threaddoctor.evidence.Evidence;
import com.geek.threaddoctor.evidence.EvidenceType;
import com.geek.threaddoctor.incident.IncidentSession;
import com.geek.threaddoctor.jstack.JstackAnalysisResult;
import com.geek.threaddoctor.jstack.LockContention;
import com.geek.threaddoctor.jstack.SuspiciousThread;
import com.geek.threaddoctor.metrics.KafkaMetrics;
import com.geek.threaddoctor.metrics.RedisMetrics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FaultPatternDetectorTest {
    private final IncidentSession session = new IncidentSession("INC-1", "t", "d", SeverityLevel.HIGH);

    @Test
    void detectsRedisPoolExhaustionFromMetrics() {
        DiagnosisContext context = new DiagnosisContext(session,
                List.of(new Evidence("EVD-1", "INC-1", EvidenceType.REDIS_METRICS, "manual", "{}", "{}")),
                Optional.empty(),
                Optional.of(new RedisMetrics(80, 80, 0, 16, 42, 203)),
                Optional.empty(),
                Optional.empty(),
                List.of());

        DetectionResult result = new RedisPoolExhaustedDetector().detect(context);

        assertThat(result.matched()).isTrue();
        assertThat(result.type()).isEqualTo(FaultPatternType.REDIS_POOL_EXHAUSTED);
        assertThat(result.supportingEvidenceIds()).contains("EVD-1");
    }

    @Test
    void detectsLockContentionFromJstackAnalysis() {
        JstackAnalysisResult analysis = new JstackAnalysisResult(2, Map.of(), Map.of(),
                List.of(new LockContention("0xabc", 2, List.of("a", "b"))),
                List.of(), List.of(), false, List.of(), List.of());
        DiagnosisContext context = new DiagnosisContext(session,
                List.of(new Evidence("EVD-J", "INC-1", EvidenceType.JSTACK, "manual", "jstack", "{}")),
                Optional.of(analysis), Optional.empty(), Optional.empty(), Optional.empty(), List.of());

        DetectionResult result = new LockContentionDetector().detect(context);

        assertThat(result.matched()).isTrue();
        assertThat(result.type()).isEqualTo(FaultPatternType.LOCK_CONTENTION);
    }

    @Test
    void detectsKafkaLag() {
        DiagnosisContext context = new DiagnosisContext(session, List.of(),
                Optional.of(new JstackAnalysisResult(1, Map.of(), Map.of(), List.of(), List.of(),
                        List.of(new SuspiciousThread("consumer", "KAFKA_BLOCKED", "at kafka.poll")), false, List.of(), List.of())),
                Optional.empty(), Optional.empty(), Optional.of(new KafkaMetrics("topic", "group", 2000, 1.0, 0)), List.of());

        DetectionResult result = new KafkaLagDetector().detect(context);

        assertThat(result.matched()).isTrue();
        assertThat(result.type()).isEqualTo(FaultPatternType.KAFKA_LAG_INCREASED);
    }
}
