package com.geek.threaddoctor.metrics;

public record DbMetrics(int maxActive, int active, int idle, int waiters) {
}
