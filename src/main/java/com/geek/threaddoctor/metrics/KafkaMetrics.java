package com.geek.threaddoctor.metrics;

public record KafkaMetrics(String topic, String groupId, long totalLag, double consumeRate, long rebalanceCount) {
}
