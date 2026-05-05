package com.geek.threaddoctor.loganalysis;

import java.util.List;

record ParsedLogFile(LogFileSummary summary, List<LogEvent> events) {
}
