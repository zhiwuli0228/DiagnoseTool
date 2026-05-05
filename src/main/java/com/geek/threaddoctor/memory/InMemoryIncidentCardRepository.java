package com.geek.threaddoctor.memory;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryIncidentCardRepository implements IncidentCardRepository {
    private final InMemoryStore<IncidentCard> store;

    public InMemoryIncidentCardRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    @Override
    public IncidentCard save(IncidentCard card) {
        return store.save(card.getId(), card);
    }

    @Override
    public List<IncidentCard> findBySessionId(String sessionId) {
        return store.findBy(card -> card.getSessionId().equals(sessionId));
    }

    @Override
    public Optional<IncidentCard> findTopBySessionIdOrderByCreatedAtDesc(String sessionId) {
        return store.findLatest(card -> card.getSessionId().equals(sessionId), IncidentCard::getCreatedAt);
    }
}
