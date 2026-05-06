/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.incident;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Repository
public class InMemoryIncidentSessionRepository implements IncidentSessionRepository {
    private final InMemoryStore<IncidentSession> store;

    /**
     * 执行业务操作。
     *
     * @param properties 配置属性
     */
    public InMemoryIncidentSessionRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    /**
     * 保存业务记录。
     *
     * @param session 业务参数
     * @return 业务处理结果
     */
    @Override
    public IncidentSession save(IncidentSession session) {
        return store.save(session.getId(), session);
    }

    /**
     * 根据标识查找记录。
     *
     * @param id 记录标识
     * @return 可能存在的匹配记录
     */
    @Override
    public Optional<IncidentSession> findById(String id) {
        return store.findById(id);
    }
}
