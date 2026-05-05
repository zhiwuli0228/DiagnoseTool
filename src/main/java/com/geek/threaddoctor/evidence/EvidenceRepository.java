package com.geek.threaddoctor.evidence;

import java.util.List;

public interface EvidenceRepository {
    Evidence save(Evidence evidence);

    List<Evidence> findBySessionId(String sessionId);
}
