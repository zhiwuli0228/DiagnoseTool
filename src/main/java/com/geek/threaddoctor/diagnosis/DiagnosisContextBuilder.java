package com.geek.threaddoctor.diagnosis;

import com.geek.threaddoctor.evidence.Evidence;
import com.geek.threaddoctor.evidence.EvidenceService;
import com.geek.threaddoctor.evidence.EvidenceType;
import com.geek.threaddoctor.incident.IncidentSession;
import com.geek.threaddoctor.incident.IncidentSessionService;
import com.geek.threaddoctor.jstack.JstackAnalysisResult;
import com.geek.threaddoctor.jstack.JstackAnalyzer;
import com.geek.threaddoctor.metrics.JvmMetrics;
import com.geek.threaddoctor.metrics.KafkaMetrics;
import com.geek.threaddoctor.metrics.RedisMetrics;
import com.geek.threaddoctor.pattern.DiagnosisContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
// 将一次会话下的散落证据整理成规则检测可直接消费的诊断上下文。
public class DiagnosisContextBuilder {
    private final IncidentSessionService incidentSessionService;
    private final EvidenceService evidenceService;
    private final JstackAnalyzer jstackAnalyzer;
    private final ObjectMapper objectMapper;

    public DiagnosisContextBuilder(IncidentSessionService incidentSessionService, EvidenceService evidenceService, JstackAnalyzer jstackAnalyzer, ObjectMapper objectMapper) {
        this.incidentSessionService = incidentSessionService;
        this.evidenceService = evidenceService;
        this.jstackAnalyzer = jstackAnalyzer;
        this.objectMapper = objectMapper;
    }

    public DiagnosisContext build(String sessionId) {
        IncidentSession session = incidentSessionService.getRequired(sessionId);
        List<Evidence> evidences = evidenceService.listBySession(sessionId);
        // jstack 文本只在上下文构建阶段解析一次，避免每个规则检测器重复扫描同一份线程栈。
        Optional<JstackAnalysisResult> jstack = evidences.stream()
                .filter(e -> e.getType() == EvidenceType.JSTACK)
                .findFirst()
                .map(e -> jstackAnalyzer.analyze(e.getContent()));
        // 缺失信息会进入诊断报告，提示用户下一步应该补充哪些证据。
        List<String> missing = new ArrayList<>();
        if (evidences.stream().noneMatch(e -> e.getType() == EvidenceType.REDIS_METRICS)) {
            missing.add("Redis metrics are missing");
        }
        if (evidences.stream().noneMatch(e -> e.getType() == EvidenceType.JSTACK)) {
            missing.add("jstack evidence is missing");
        }
        Optional<RedisMetrics> redis = readMetric(evidences, EvidenceType.REDIS_METRICS, RedisMetrics.class);
        Optional<JvmMetrics> jvm = readMetric(evidences, EvidenceType.JVM_METRICS, JvmMetrics.class);
        Optional<KafkaMetrics> kafka = readMetric(evidences, EvidenceType.KAFKA_METRICS, KafkaMetrics.class);
        return new DiagnosisContext(session, evidences, jstack, redis, jvm, kafka, missing);
    }

    private <T> Optional<T> readMetric(List<Evidence> evidences, EvidenceType type, Class<T> targetType) {
        // 同类指标当前只取第一份，MVP 假设一次会话上传的是一个关键时间点快照。
        return evidences.stream()
                .filter(e -> e.getType() == type)
                .findFirst()
                .flatMap(e -> parse(e.getContent(), targetType));
    }

    private <T> Optional<T> parse(String content, Class<T> targetType) {
        try {
            return Optional.of(objectMapper.readValue(content, targetType));
        } catch (Exception ignored) {
            // 指标格式错误不应阻断整次诊断，规则层会把该指标视为缺失。
            return Optional.empty();
        }
    }
}
