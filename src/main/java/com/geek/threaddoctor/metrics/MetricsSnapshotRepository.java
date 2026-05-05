package com.geek.threaddoctor.metrics;

import java.util.List;

public interface MetricsSnapshotRepository {
    MetricsSnapshot save(MetricsSnapshot snapshot);

    List<MetricsSnapshot> findBySessionId(String sessionId);
}
