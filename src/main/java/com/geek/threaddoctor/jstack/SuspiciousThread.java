package com.geek.threaddoctor.jstack;

public record SuspiciousThread(String threadName, String reason, String topFrame) {
}
