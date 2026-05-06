/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.diagnosis;

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
public class InMemoryDiagnosisProgressRepository implements DiagnosisProgressRepository {
    private final InMemoryStore<DiagnosisProgress> store;

    /**
     * 执行业务操作。
     *
     * @param properties 配置属性
     */
    public InMemoryDiagnosisProgressRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    /**
     * 保存业务记录。
     *
     * @param progress 业务参数
     * @return 业务处理结果
     */
    @Override
    public DiagnosisProgress save(DiagnosisProgress progress) {
        return store.save(progress.getSessionId(), progress);
    }

    /**
     * 根据会话标识查找记录。
     *
     * @param sessionId 会话标识
     * @return 可能存在的匹配记录
     */
    @Override
    public Optional<DiagnosisProgress> findBySessionId(String sessionId) {
        return store.findById(sessionId);
    }
}
