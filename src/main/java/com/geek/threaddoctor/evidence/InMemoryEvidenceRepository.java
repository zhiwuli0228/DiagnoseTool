package com.geek.threaddoctor.evidence;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryEvidenceRepository implements EvidenceRepository {
    private final InMemoryStore<Evidence> store;

    public InMemoryEvidenceRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    @Override
    public Evidence save(Evidence evidence) {
        return store.save(evidence.getId(), evidence);
    }

    @Override
    public List<Evidence> findBySessionId(String sessionId) {
        return store.findBy(evidence -> evidence.getSessionId().equals(sessionId));
    }
}
