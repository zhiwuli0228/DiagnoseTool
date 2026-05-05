package com.geek.threaddoctor.recovery;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryRecoveryActionRepository implements RecoveryActionRepository {
    private final InMemoryStore<RecoveryAction> store;

    public InMemoryRecoveryActionRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    @Override
    public RecoveryAction save(RecoveryAction action) {
        return store.save(action.getId(), action);
    }

    @Override
    public List<RecoveryAction> saveAll(List<RecoveryAction> actions) {
        actions.forEach(this::save);
        return actions;
    }

    @Override
    public Optional<RecoveryAction> findById(String id) {
        return store.findById(id);
    }

    @Override
    public List<RecoveryAction> findBySessionId(String sessionId) {
        return store.findBy(action -> action.getSessionId().equals(sessionId));
    }
}
