package com.geek.threaddoctor.metrics;

public record RedisMetrics(int maxActive, int active, int idle, int waiters, long borrowTimeoutCount, long commandTimeoutCount) {
}
