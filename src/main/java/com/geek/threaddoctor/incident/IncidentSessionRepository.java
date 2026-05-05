package com.geek.threaddoctor.incident;

import java.util.Optional;

public interface IncidentSessionRepository {
    IncidentSession save(IncidentSession session);

    Optional<IncidentSession> findById(String id);
}
