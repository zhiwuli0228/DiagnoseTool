/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.evidence;

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
public class EvidenceService {
    private final EvidenceRepository repository;
    private final IncidentSessionService incidentSessionService;

    /**
     * 执行业务操作。
     *
     * @param repository 仓储依赖
     * @param incidentSessionService 业务服务依赖
     */
    public EvidenceService(EvidenceRepository repository, IncidentSessionService incidentSessionService) {
        this.repository = repository;
        this.incidentSessionService = incidentSessionService;
    }

    /**
     * 上传并保存证据。
     *
     * @param sessionId 会话标识
     * @param type 类型
     * @param source 来源
     * @param content 内容
     * @param metadataJson 元数据
     * @return 已保存的证据
     */
    public Evidence upload(String sessionId, EvidenceType type, String source, String content, String metadataJson) {
        incidentSessionService.getRequired(sessionId);
        // 证据内容只缓存到当前会话，用于后续诊断上下文构建。
        if (type == null || content == null || content.isBlank()) {
            throw new IllegalArgumentException("Evidence type and content are required");
        }
        return repository.save(new Evidence("EVD-" + UUID.randomUUID(), sessionId, type, source, content, metadataJson));
    }

    /**
     * 执行业务操作。
     *
     * @param sessionId 会话标识
     * @return 指定会话的记录集合
     */
    public List<Evidence> listBySession(String sessionId) {
        incidentSessionService.getRequired(sessionId);
        return repository.findBySessionId(sessionId);
    }
}
