package com.geek.threaddoctor.jstack;

import java.util.List;
import java.util.Map;

public record JstackAnalysisResult(
        int totalThreads,
        Map<Thread.State, Integer> stateCount,
        Map<String, Integer> threadGroups,
        List<LockContention> lockContentions,
        List<HotStack> hotStacks,
        List<SuspiciousThread> suspiciousThreads,
        boolean deadlockDetected,
        List<String> deadlockDetails,
        List<ThreadDumpBlock> threads) {
}
