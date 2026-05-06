/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.metrics;

import com.geek.threaddoctor.incident.IncidentSessionService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Service
public class MetricsSnapshotService {
    private final MetricsSnapshotRepository repository;
    private final IncidentSessionService incidentSessionService;

    /**
     * 执行业务操作。
     *
     * @param repository 仓储依赖
     * @param incidentSessionService 业务服务依赖
     */
    public MetricsSnapshotService(MetricsSnapshotRepository repository, IncidentSessionService incidentSessionService) {
        this.repository = repository;
        this.incidentSessionService = incidentSessionService;
    }

    /**
     * 保存业务记录。
     *
     * @param sessionId 会话标识
     * @param jvm 业务参数
     * @param redis 业务参数
     * @param kafka 业务参数
     * @param db 业务参数
     * @return 业务处理结果
     */
    public MetricsSnapshot save(String sessionId, String jvm, String redis, String kafka, String db) {
        // 指标快照只缓存当前会话，缓存缺失由诊断上下文显式表达。
        incidentSessionService.getRequired(sessionId);
        return repository.save(new MetricsSnapshot("MTR-" + UUID.randomUUID(), sessionId, jvm, redis, kafka, db));
    }

    /**
     * 执行业务操作。
     *
     * @param sessionId 会话标识
     * @return 指定会话的记录集合
     */
    public List<MetricsSnapshot> listBySession(String sessionId) {
        incidentSessionService.getRequired(sessionId);
        return repository.findBySessionId(sessionId);
    }
}
