/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.metrics;

import java.util.List;

/**
 * 定义组件对外契约。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public interface MetricsSnapshotRepository {
    MetricsSnapshot save(MetricsSnapshot snapshot);

    List<MetricsSnapshot> findBySessionId(String sessionId);
}
