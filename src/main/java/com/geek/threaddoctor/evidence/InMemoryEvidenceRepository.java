/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.evidence;

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
public class InMemoryEvidenceRepository implements EvidenceRepository {
    private final InMemoryStore<Evidence> store;

    /**
     * 执行业务操作。
     *
     * @param properties 配置属性
     */
    public InMemoryEvidenceRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    /**
     * 保存业务记录。
     *
     * @param evidence 业务参数
     * @return 业务处理结果
     */
    @Override
    public Evidence save(Evidence evidence) {
        return store.save(evidence.getId(), evidence);
    }

    /**
     * 根据会话标识查找记录。
     *
     * @param sessionId 会话标识
     * @return 匹配的记录集合
     */
    @Override
    public List<Evidence> findBySessionId(String sessionId) {
        return store.findBy(evidence -> evidence.getSessionId().equals(sessionId));
    }
}
