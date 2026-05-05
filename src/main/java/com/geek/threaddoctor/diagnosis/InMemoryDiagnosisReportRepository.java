package com.geek.threaddoctor.diagnosis;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryDiagnosisReportRepository implements DiagnosisReportRepository {
    private final InMemoryStore<DiagnosisReport> store;

    public InMemoryDiagnosisReportRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    @Override
    public DiagnosisReport save(DiagnosisReport report) {
        return store.save(report.getId(), report);
    }

    @Override
    public Optional<DiagnosisReport> findTopBySessionIdOrderByGeneratedAtDesc(String sessionId) {
        return store.findLatest(report -> report.getSessionId().equals(sessionId), DiagnosisReport::getGeneratedAt);
    }
}
