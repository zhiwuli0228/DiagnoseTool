/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.recovery;

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
public class InMemoryRecoveryActionRepository implements RecoveryActionRepository {
    private final InMemoryStore<RecoveryAction> store;

    /**
     * 执行业务操作。
     *
     * @param properties 配置属性
     */
    public InMemoryRecoveryActionRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    /**
     * 保存业务记录。
     *
     * @param action 业务参数
     * @return 业务处理结果
     */
    @Override
    public RecoveryAction save(RecoveryAction action) {
        return store.save(action.getId(), action);
    }

    /**
     * 批量保存业务记录。
     *
     * @param actions 业务参数
     * @return 匹配的记录集合
     */
    @Override
    public List<RecoveryAction> saveAll(List<RecoveryAction> actions) {
        actions.forEach(this::save);
        return actions;
    }

    /**
     * 根据标识查找记录。
     *
     * @param id 记录标识
     * @return 可能存在的匹配记录
     */
    @Override
    public Optional<RecoveryAction> findById(String id) {
        return store.findById(id);
    }

    /**
     * 根据会话标识查找记录。
     *
     * @param sessionId 会话标识
     * @return 匹配的记录集合
     */
    @Override
    public List<RecoveryAction> findBySessionId(String sessionId) {
        return store.findBy(action -> action.getSessionId().equals(sessionId));
    }
}
