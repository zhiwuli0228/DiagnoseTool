package com.geek.threaddoctor.metrics;

import com.geek.threaddoctor.incident.IncidentSessionService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MetricsSnapshotService {
    private final MetricsSnapshotRepository repository;
    private final IncidentSessionService incidentSessionService;

    public MetricsSnapshotService(MetricsSnapshotRepository repository, IncidentSessionService incidentSessionService) {
        this.repository = repository;
        this.incidentSessionService = incidentSessionService;
    }

    public MetricsSnapshot save(String sessionId, String jvm, String redis, String kafka, String db) {
        // 指标快照只缓存当前会话，缓存缺失由诊断上下文显式表达。
        incidentSessionService.getRequired(sessionId);
        return repository.save(new MetricsSnapshot("MTR-" + UUID.randomUUID(), sessionId, jvm, redis, kafka, db));
    }

    public List<MetricsSnapshot> listBySession(String sessionId) {
        incidentSessionService.getRequired(sessionId);
        return repository.findBySessionId(sessionId);
    }
}
