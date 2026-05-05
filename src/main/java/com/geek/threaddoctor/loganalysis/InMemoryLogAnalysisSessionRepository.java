package com.geek.threaddoctor.loganalysis;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLogAnalysisSessionRepository {
    private final InMemoryStore<LogAnalysisSession> store;

    public InMemoryLogAnalysisSessionRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    public LogAnalysisSession save(LogAnalysisSession session) {
        return store.save(session.getId(), session);
    }

    public Optional<LogAnalysisSession> findById(String id) {
        return store.findById(id);
    }
}
