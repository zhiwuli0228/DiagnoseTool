package com.geek.threaddoctor.metrics;

public record JvmMetrics(long heapUsed, long heapMax, long oldGenUsed, long oldGenMax, int liveThreads, long youngGcCount, long fullGcCount, double processCpuLoad) {
}
