/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.incident;

import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.common.SeverityLevel;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Service
public class IncidentSessionService {
    private final IncidentSessionRepository repository;

    /**
     * 执行业务操作。
     *
     * @param repository 仓储依赖
     */
    public IncidentSessionService(IncidentSessionRepository repository) {
        this.repository = repository;
    }

    /**
     * 执行业务操作。
     *
     * @param title 标题
     * @param description 描述信息
     * @param severity 严重级别
     * @return 业务处理结果
     */
    public IncidentSession create(String title, String description, SeverityLevel severity) {
        // 会话只进入缓存，标题用于用户在当前诊断闭环中识别故障。
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Incident title is required");
        }
        return repository.save(new IncidentSession("INC-" + UUID.randomUUID(), title, description, severity));
    }

    /**
     * 获取必须存在的事件会话。
     *
     * @param sessionId 会话标识
     * @return 事件会话
     */
    public IncidentSession getRequired(String sessionId) {
        return repository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident session not found: " + sessionId));
    }

    /**
     * 更新事件会话状态。
     *
     * @param sessionId 会话标识
     * @param status 目标状态
     * @return 更新后的事件会话
     */
    public IncidentSession markStatus(String sessionId, IncidentStatus status) {
        IncidentSession session = getRequired(sessionId);
        session.markStatus(status);
        return repository.save(session);
    }
}
