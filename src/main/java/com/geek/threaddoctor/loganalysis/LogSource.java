package com.geek.threaddoctor.loganalysis;

public record LogSource(String id, LogSourceType type, String name, long byteSize) {
}
