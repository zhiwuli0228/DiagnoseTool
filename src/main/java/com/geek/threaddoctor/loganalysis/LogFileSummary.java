package com.geek.threaddoctor.loganalysis;

public record LogFileSummary(String sourceFile, long byteSize, int lineCount, int eventCount, int unparsedCount) {
}
