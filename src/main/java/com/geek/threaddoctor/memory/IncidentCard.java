/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.memory;

import java.time.LocalDateTime;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class IncidentCard {
    private String id;
    private String sessionId;
    private String markdown;
    private String tags;
    private LocalDateTime createdAt;

    protected IncidentCard() {
    }

    /**
     * 执行业务操作。
     *
     * @param id 记录标识
     * @param sessionId 会话标识
     * @param markdown 业务参数
     * @param tags 业务参数
     */
    public IncidentCard(String id, String sessionId, String markdown, String tags) {
        this.id = id;
        this.sessionId = sessionId;
        this.markdown = markdown;
        this.tags = tags;
        this.createdAt = LocalDateTime.now();
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
     * @return 会话标识
     */
    public String getSessionId() { return sessionId; }
    /**
     * 获取业务字段。
     *
     * @return 文档内容
     */
    public String getMarkdown() { return markdown; }
    /**
     * 获取业务字段。
     *
     * @return 标签集合
     */
    public String getTags() { return tags; }
    /**
     * 获取业务字段。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
