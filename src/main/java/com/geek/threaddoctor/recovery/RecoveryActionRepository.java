package com.geek.threaddoctor.recovery;

import java.util.List;
import java.util.Optional;

public interface RecoveryActionRepository {
    RecoveryAction save(RecoveryAction action);

    List<RecoveryAction> saveAll(List<RecoveryAction> actions);

    Optional<RecoveryAction> findById(String id);

    List<RecoveryAction> findBySessionId(String sessionId);
}
