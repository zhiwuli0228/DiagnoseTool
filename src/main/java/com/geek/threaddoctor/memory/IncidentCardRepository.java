/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.memory;

import java.util.List;
import java.util.Optional;

/**
 * 定义组件对外契约。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public interface IncidentCardRepository {
    IncidentCard save(IncidentCard card);

    List<IncidentCard> findBySessionId(String sessionId);

    Optional<IncidentCard> findTopBySessionIdOrderByCreatedAtDesc(String sessionId);
}
