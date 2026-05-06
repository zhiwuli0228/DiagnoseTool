/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

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
public class InMemoryLogAnalysisSessionRepository {
    private final InMemoryStore<LogAnalysisSession> store;

    /**
     * 执行业务操作。
     *
     * @param properties 配置属性
     */
    public InMemoryLogAnalysisSessionRepository(CacheProperties properties) {
        this.store = new InMemoryStore<>(properties);
    }

    /**
     * 保存业务记录。
     *
     * @param session 业务参数
     * @return 业务处理结果
     */
    public LogAnalysisSession save(LogAnalysisSession session) {
        return store.save(session.getId(), session);
    }

    /**
     * 根据标识查找记录。
     *
     * @param id 记录标识
     * @return 可能存在的匹配记录
     */
    public Optional<LogAnalysisSession> findById(String id) {
        return store.findById(id);
    }
}
