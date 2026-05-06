/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.recovery;

import java.util.List;
import java.util.Optional;

/**
 * 定义组件对外契约。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public interface RecoveryActionRepository {
    RecoveryAction save(RecoveryAction action);

    List<RecoveryAction> saveAll(List<RecoveryAction> actions);

    Optional<RecoveryAction> findById(String id);

    List<RecoveryAction> findBySessionId(String sessionId);
}
