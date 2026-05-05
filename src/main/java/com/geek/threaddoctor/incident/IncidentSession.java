package com.geek.threaddoctor.incident;

import com.geek.threaddoctor.common.SeverityLevel;
import java.time.LocalDateTime;

public class IncidentSession {
    private String id;
    private String title;
    private String description;
    private IncidentStatus status;
    private SeverityLevel severity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected IncidentSession() {
    }

    public IncidentSession(String id, String title, String description, SeverityLevel severity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = IncidentStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public IncidentStatus getStatus() { return status; }
    public SeverityLevel getSeverity() { return severity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void markStatus(IncidentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
