package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogAnalysisSession {
    private final String id;
    private LogAnalysisStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<LogSource> sources = new ArrayList<>();
    private final List<LogFileSummary> fileSummaries = new ArrayList<>();
    private final List<LogAnalysisError> errors = new ArrayList<>();
    private final List<LogEvent> events = new ArrayList<>();

    public LogAnalysisSession(String id, LocalDateTime now) {
        this.id = id;
        this.status = LogAnalysisStatus.CREATED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getId() {
        return id;
    }

    public LogAnalysisStatus getStatus() {
        return status;
    }

    public void setStatus(LogAnalysisStatus status) {
        this.status = status;
        touch();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<LogSource> getSources() {
        return List.copyOf(sources);
    }

    public List<LogFileSummary> getFileSummaries() {
        return List.copyOf(fileSummaries);
    }

    public List<LogAnalysisError> getErrors() {
        return List.copyOf(errors);
    }

    public int getEventCount() {
        return events.size();
    }

    List<LogEvent> events() {
        return events;
    }

    void addSource(LogSource source) {
        sources.add(source);
        touch();
    }

    void addFileSummary(LogFileSummary summary) {
        fileSummaries.add(summary);
        touch();
    }

    void addError(LogAnalysisError error) {
        errors.add(error);
        status = LogAnalysisStatus.FAILED;
        touch();
    }

    void replaceEvents(List<LogEvent> newEvents) {
        events.clear();
        events.addAll(newEvents);
        status = LogAnalysisStatus.PROCESSED;
        touch();
    }

    private void touch() {
        updatedAt = LocalDateTime.now();
    }
}
