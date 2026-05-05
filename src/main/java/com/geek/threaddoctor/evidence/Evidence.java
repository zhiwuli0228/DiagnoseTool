package com.geek.threaddoctor.evidence;

import java.time.LocalDateTime;

public class Evidence {
    private String id;
    private String sessionId;
    private EvidenceType type;
    private String source;
    private String content;
    private String parsedSummary;
    private String metadataJson;
    private LocalDateTime collectedAt;

    protected Evidence() {
    }

    public Evidence(String id, String sessionId, EvidenceType type, String source, String content, String metadataJson) {
        this.id = id;
        this.sessionId = sessionId;
        this.type = type;
        this.source = source;
        this.content = content;
        this.metadataJson = metadataJson;
        this.collectedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public EvidenceType getType() { return type; }
    public String getSource() { return source; }
    public String getContent() { return content; }
    public String getParsedSummary() { return parsedSummary; }
    public String getMetadataJson() { return metadataJson; }
    public LocalDateTime getCollectedAt() { return collectedAt; }

    public void setParsedSummary(String parsedSummary) {
        this.parsedSummary = parsedSummary;
    }
}
