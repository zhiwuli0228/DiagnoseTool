/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.incident;

import com.geek.threaddoctor.common.SeverityLevel;
import java.time.LocalDateTime;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class IncidentSession {
    private String id;
    private String title;
    private String description;
    private IncidentStatus status;
    private SeverityLevel severity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected IncidentSession() {
    }

    /**
     * 执行业务操作。
     *
     * @param id 记录标识
     * @param title 标题
     * @param description 描述信息
     * @param severity 严重级别
     */
    public IncidentSession(String id, String title, String description, SeverityLevel severity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = IncidentStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * 获取业务字段。
     *
     * @return 记录标识
     */
    public String getId() { return id; }
    /**
     * 获取业务字段。
     *
     * @return 标题
     */
    public String getTitle() { return title; }
    /**
     * 获取业务字段。
     *
     * @return 描述信息
     */
    public String getDescription() { return description; }
    /**
     * 获取业务字段。
     *
     * @return 状态
     */
    public IncidentStatus getStatus() { return status; }
    /**
     * 获取业务字段。
     *
     * @return 严重级别
     */
    public SeverityLevel getSeverity() { return severity; }
    /**
     * 获取业务字段。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    /**
     * 获取业务字段。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /**
     * 更新事件会话状态。
     *
     * @param status 目标状态
     */
    public void markStatus(IncidentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
