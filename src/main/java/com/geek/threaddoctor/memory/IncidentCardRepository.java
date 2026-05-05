package com.geek.threaddoctor.memory;

import java.util.List;
import java.util.Optional;

public interface IncidentCardRepository {
    IncidentCard save(IncidentCard card);

    List<IncidentCard> findBySessionId(String sessionId);

    Optional<IncidentCard> findTopBySessionIdOrderByCreatedAtDesc(String sessionId);
}
