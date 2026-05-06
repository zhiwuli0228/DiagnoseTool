/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.memory;

import com.geek.threaddoctor.common.cache.CacheProperties;
import com.geek.threaddoctor.common.cache.InMemoryStore;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Repository
public class InMemoryIncidentCardRepository implements IncidentCardRepository {
    private final InMemoryStore<IncidentCard> store;

    /**
     * 执行业务操作。
     *
     * @param properties 配置属性
     */
    public InMemoryIncidentCardRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    /**
     * 保存业务记录。
     *
     * @param card 业务参数
     * @return 业务处理结果
     */
    @Override
    public IncidentCard save(IncidentCard card) {
        return store.save(card.getId(), card);
    }

    /**
     * 根据会话标识查找记录。
     *
     * @param sessionId 会话标识
     * @return 匹配的记录集合
     */
    @Override
    public List<IncidentCard> findBySessionId(String sessionId) {
        return store.findBy(card -> card.getSessionId().equals(sessionId));
    }

    /**
     * 根据会话标识查找最新创建的记录。
     *
     * @param sessionId 会话标识
     * @return 可能存在的匹配记录
     */
    @Override
    public Optional<IncidentCard> findTopBySessionIdOrderByCreatedAtDesc(String sessionId) {
        return store.findLatest(card -> card.getSessionId().equals(sessionId), IncidentCard::getCreatedAt);
    }
}
