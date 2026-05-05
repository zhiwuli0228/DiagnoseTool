package com.geek.threaddoctor.metrics;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryMetricsSnapshotRepository implements MetricsSnapshotRepository {
    private final InMemoryStore<MetricsSnapshot> store;

    public InMemoryMetricsSnapshotRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    @Override
    public MetricsSnapshot save(MetricsSnapshot snapshot) {
        return store.save(snapshot.getId(), snapshot);
    }

    @Override
    public List<MetricsSnapshot> findBySessionId(String sessionId) {
        return store.findBy(snapshot -> snapshot.getSessionId().equals(sessionId));
    }
}
