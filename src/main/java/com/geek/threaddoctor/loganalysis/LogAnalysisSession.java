/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class LogAnalysisSession {
    private final String id;
    private LogAnalysisStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<LogSource> sources = new ArrayList<>();
    private final List<LogFileSummary> fileSummaries = new ArrayList<>();
    private final List<LogAnalysisError> errors = new ArrayList<>();
    private final List<LogEvent> events = new ArrayList<>();

    /**
     * 执行业务操作。
     *
     * @param id 记录标识
     * @param now 业务参数
     */
    public LogAnalysisSession(String id, LocalDateTime now) {
        this.id = id;
        this.status = LogAnalysisStatus.CREATED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 获取业务字段。
     *
     * @return 记录标识
     */
    public String getId() {
        return id;
    }

    /**
     * 获取业务字段。
     *
     * @return 状态
     */
    public LogAnalysisStatus getStatus() {
        return status;
    }

    /**
     * 更新业务字段。
     *
     * @param status 目标状态
     */
    public void setStatus(LogAnalysisStatus status) {
        this.status = status;
        touch();
    }

    /**
     * 获取业务字段。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取业务字段。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 获取业务字段。
     *
     * @return 匹配的记录集合
     */
    public List<LogSource> getSources() {
        return List.copyOf(sources);
    }

    /**
     * 获取业务字段。
     *
     * @return 匹配的记录集合
     */
    public List<LogFileSummary> getFileSummaries() {
        return List.copyOf(fileSummaries);
    }

    /**
     * 获取业务字段。
     *
     * @return 匹配的记录集合
     */
    public List<LogAnalysisError> getErrors() {
        return List.copyOf(errors);
    }

    /**
     * 获取业务字段。
     *
     * @return 业务处理结果
     */
    public int getEventCount() {
        return events.size();
    }

    List<LogEvent> events() {
        return events;
    }

    void addSource(LogSource source) {
        sources.add(source);
        touch();
    }

    void addFileSummary(LogFileSummary summary) {
        fileSummaries.add(summary);
        touch();
    }

    void addError(LogAnalysisError error) {
        errors.add(error);
        status = LogAnalysisStatus.FAILED;
        touch();
    }

    void replaceEvents(List<LogEvent> newEvents) {
        events.clear();
        events.addAll(newEvents);
        status = LogAnalysisStatus.PROCESSED;
        touch();
    }

    private void touch() {
        updatedAt = LocalDateTime.now();
    }
}
