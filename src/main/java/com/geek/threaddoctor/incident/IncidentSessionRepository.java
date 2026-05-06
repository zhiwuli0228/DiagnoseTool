/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.incident;

import java.util.Optional;

/**
 * 定义组件对外契约。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public interface IncidentSessionRepository {
    IncidentSession save(IncidentSession session);

    Optional<IncidentSession> findById(String id);
}
