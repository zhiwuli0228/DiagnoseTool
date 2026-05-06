/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.diagnosis;

import java.util.Optional;

/**
 * 定义组件对外契约。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public interface DiagnosisProgressRepository {
    DiagnosisProgress save(DiagnosisProgress progress);

    Optional<DiagnosisProgress> findBySessionId(String sessionId);
}
