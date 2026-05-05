package com.geek.threaddoctor.metrics;

import java.time.LocalDateTime;

public class MetricsSnapshot {
    private String id;
    private String sessionId;
    private LocalDateTime timestamp;
    private String jvmMetricsJson;
    private String redisMetricsJson;
    private String kafkaMetricsJson;
    private String dbMetricsJson;

    protected MetricsSnapshot() {
    }

    public MetricsSnapshot(String id, String sessionId, String jvmMetricsJson, String redisMetricsJson, String kafkaMetricsJson, String dbMetricsJson) {
        this.id = id;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
        this.jvmMetricsJson = jvmMetricsJson;
        this.redisMetricsJson = redisMetricsJson;
        this.kafkaMetricsJson = kafkaMetricsJson;
        this.dbMetricsJson = dbMetricsJson;
    }

    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getJvmMetricsJson() { return jvmMetricsJson; }
    public String getRedisMetricsJson() { return redisMetricsJson; }
    public String getKafkaMetricsJson() { return kafkaMetricsJson; }
    public String getDbMetricsJson() { return dbMetricsJson; }
}
