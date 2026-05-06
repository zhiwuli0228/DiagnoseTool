/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.metrics;

import java.time.LocalDateTime;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class MetricsSnapshot {
    private String id;
    private String sessionId;
    private LocalDateTime timestamp;
    private String jvmMetricsJson;
    private String redisMetricsJson;
    private String kafkaMetricsJson;
    private String dbMetricsJson;

    protected MetricsSnapshot() {
    }

    /**
     * 执行业务操作。
     *
     * @param id 记录标识
     * @param sessionId 会话标识
     * @param jvmMetricsJson 数据内容
     * @param redisMetricsJson 数据内容
     * @param kafkaMetricsJson 数据内容
     * @param dbMetricsJson 数据内容
     */
    public MetricsSnapshot(String id, String sessionId, String jvmMetricsJson, String redisMetricsJson, String kafkaMetricsJson, String dbMetricsJson) {
        this.id = id;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
        this.jvmMetricsJson = jvmMetricsJson;
        this.redisMetricsJson = redisMetricsJson;
        this.kafkaMetricsJson = kafkaMetricsJson;
        this.dbMetricsJson = dbMetricsJson;
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
     * @return 业务处理结果
     */
    public LocalDateTime getTimestamp() { return timestamp; }
    /**
     * 获取业务字段。
     *
     * @return 文本结果
     */
    public String getJvmMetricsJson() { return jvmMetricsJson; }
    /**
     * 获取业务字段。
     *
     * @return 文本结果
     */
    public String getRedisMetricsJson() { return redisMetricsJson; }
    /**
     * 获取业务字段。
     *
     * @return 文本结果
     */
    public String getKafkaMetricsJson() { return kafkaMetricsJson; }
    /**
     * 获取业务字段。
     *
     * @return 文本结果
     */
    public String getDbMetricsJson() { return dbMetricsJson; }
}
