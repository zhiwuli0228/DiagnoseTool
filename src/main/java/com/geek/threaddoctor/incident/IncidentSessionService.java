package com.geek.threaddoctor.incident;

import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.common.SeverityLevel;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class IncidentSessionService {
    private final IncidentSessionRepository repository;

    public IncidentSessionService(IncidentSessionRepository repository) {
        this.repository = repository;
    }

    public IncidentSession create(String title, String description, SeverityLevel severity) {
        // 会话只进入缓存，标题用于用户在当前诊断闭环中识别故障。
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Incident title is required");
        }
        return repository.save(new IncidentSession("INC-" + UUID.randomUUID(), title, description, severity));
    }

    public IncidentSession getRequired(String sessionId) {
        return repository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident session not found: " + sessionId));
    }

    public IncidentSession markStatus(String sessionId, IncidentStatus status) {
        IncidentSession session = getRequired(sessionId);
        session.markStatus(status);
        return repository.save(session);
    }
}
