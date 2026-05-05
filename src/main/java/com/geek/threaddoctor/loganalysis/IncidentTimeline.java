package com.geek.threaddoctor.loganalysis;

import java.util.List;

public record IncidentTimeline(String sessionId, List<TimelineEvent> events) {
}
