package com.geek.threaddoctor.jstack;

import java.util.List;

public record LockContention(String lockId, int waitingThreadCount, List<String> threadNames) {
}
