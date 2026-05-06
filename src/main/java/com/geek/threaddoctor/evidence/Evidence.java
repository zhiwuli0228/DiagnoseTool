/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.evidence;

import java.time.LocalDateTime;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class Evidence {
    private String id;
    private String sessionId;
    private EvidenceType type;
    private String source;
    private String content;
    private String parsedSummary;
    private String metadataJson;
    private LocalDateTime collectedAt;

    protected Evidence() {
    }

    /**
     * 执行业务操作。
     *
     * @param id 记录标识
     * @param sessionId 会话标识
     * @param type 类型
     * @param source 来源
     * @param content 内容
     * @param metadataJson 元数据
     */
    public Evidence(String id, String sessionId, EvidenceType type, String source, String content, String metadataJson) {
        this.id = id;
        this.sessionId = sessionId;
        this.type = type;
        this.source = source;
        this.content = content;
        this.metadataJson = metadataJson;
        this.collectedAt = LocalDateTime.now();
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
     * @return 类型
     */
    public EvidenceType getType() { return type; }
    /**
     * 获取业务字段。
     *
     * @return 来源
     */
    public String getSource() { return source; }
    /**
     * 获取业务字段。
     *
     * @return 内容
     */
    public String getContent() { return content; }
    /**
     * 获取业务字段。
     *
     * @return 解析摘要
     */
    public String getParsedSummary() { return parsedSummary; }
    /**
     * 获取业务字段。
     *
     * @return 元数据
     */
    public String getMetadataJson() { return metadataJson; }
    /**
     * 获取业务字段。
     *
     * @return 业务处理结果
     */
    public LocalDateTime getCollectedAt() { return collectedAt; }

    /**
     * 更新业务字段。
     *
     * @param parsedSummary 解析摘要
     */
    public void setParsedSummary(String parsedSummary) {
        this.parsedSummary = parsedSummary;
    }
}
