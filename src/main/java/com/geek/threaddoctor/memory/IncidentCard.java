package com.geek.threaddoctor.memory;

import java.time.LocalDateTime;

public class IncidentCard {
    private String id;
    private String sessionId;
    private String markdown;
    private String tags;
    private LocalDateTime createdAt;

    protected IncidentCard() {
    }

    public IncidentCard(String id, String sessionId, String markdown, String tags) {
        this.id = id;
        this.sessionId = sessionId;
        this.markdown = markdown;
        this.tags = tags;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getMarkdown() { return markdown; }
    public String getTags() { return tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
