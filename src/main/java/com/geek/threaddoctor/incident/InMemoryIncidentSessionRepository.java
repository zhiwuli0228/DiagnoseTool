package com.geek.threaddoctor.incident;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryIncidentSessionRepository implements IncidentSessionRepository {
    private final InMemoryStore<IncidentSession> store;

    public InMemoryIncidentSessionRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    @Override
    public IncidentSession save(IncidentSession session) {
        return store.save(session.getId(), session);
    }

    @Override
    public Optional<IncidentSession> findById(String id) {
        return store.findById(id);
    }
}
