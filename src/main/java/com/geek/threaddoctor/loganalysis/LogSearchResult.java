package com.geek.threaddoctor.loganalysis;

import java.util.List;

public record LogSearchResult(int totalMatched, int limit, List<LogEvent> events) {
}
