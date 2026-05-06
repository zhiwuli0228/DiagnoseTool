/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.metrics;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Repository
public class InMemoryMetricsSnapshotRepository implements MetricsSnapshotRepository {
    private final InMemoryStore<MetricsSnapshot> store;

    /**
     * 执行业务操作。
     *
     * @param properties 配置属性
     */
    public InMemoryMetricsSnapshotRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    /**
     * 保存业务记录。
     *
     * @param snapshot 业务参数
     * @return 业务处理结果
     */
    @Override
    public MetricsSnapshot save(MetricsSnapshot snapshot) {
        return store.save(snapshot.getId(), snapshot);
    }

    /**
     * 根据会话标识查找记录。
     *
     * @param sessionId 会话标识
     * @return 匹配的记录集合
     */
    @Override
    public List<MetricsSnapshot> findBySessionId(String sessionId) {
        return store.findBy(snapshot -> snapshot.getSessionId().equals(sessionId));
    }
}
