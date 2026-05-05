package com.geek.threaddoctor.diagnosis;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryDiagnosisProgressRepository implements DiagnosisProgressRepository {
    private final InMemoryStore<DiagnosisProgress> store;

    public InMemoryDiagnosisProgressRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    @Override
    public DiagnosisProgress save(DiagnosisProgress progress) {
        return store.save(progress.getSessionId(), progress);
    }

    @Override
    public Optional<DiagnosisProgress> findBySessionId(String sessionId) {
        return store.findById(sessionId);
    }
}
