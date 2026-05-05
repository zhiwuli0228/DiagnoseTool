package com.geek.threaddoctor.evidence;

import com.geek.threaddoctor.incident.IncidentSessionService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EvidenceService {
    private final EvidenceRepository repository;
    private final IncidentSessionService incidentSessionService;

    public EvidenceService(EvidenceRepository repository, IncidentSessionService incidentSessionService) {
        this.repository = repository;
        this.incidentSessionService = incidentSessionService;
    }

    public Evidence upload(String sessionId, EvidenceType type, String source, String content, String metadataJson) {
        incidentSessionService.getRequired(sessionId);
        // 证据内容只缓存到当前会话，用于后续诊断上下文构建。
        if (type == null || content == null || content.isBlank()) {
            throw new IllegalArgumentException("Evidence type and content are required");
        }
        return repository.save(new Evidence("EVD-" + UUID.randomUUID(), sessionId, type, source, content, metadataJson));
    }

    public List<Evidence> listBySession(String sessionId) {
        incidentSessionService.getRequired(sessionId);
        return repository.findBySessionId(sessionId);
    }
}
