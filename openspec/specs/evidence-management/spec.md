# evidence-management Specification

## Purpose
TBD - created by archiving change add-thread-doctor-mvp. Update Purpose after archive.
## Requirements
### Requirement: 缓存型证据管理
系统 SHALL 允许用户为指定故障会话上传证据，证据类型至少包括 `ALERT_TEXT`、`LOG_SNIPPET`、`JSTACK`、`JVM_METRICS`、`REDIS_METRICS`、`KAFKA_METRICS`、`DB_METRICS`、`CONFIG_CHANGE` 和 `MANUAL_NOTE`。系统 MUST 将证据保存在应用缓存中，并 MUST NOT 写入数据库。

#### Scenario: 上传日志证据
- **WHEN** 用户为缓存中的会话上传日志片段
- **THEN** 系统 MUST 缓存该证据并返回证据 ID、类型和会话 ID

#### Scenario: 上传 Redis 指标证据
- **WHEN** 用户上传 Redis 指标证据并包含 active、idle、waiters 等字段
- **THEN** 系统 MUST 将其归一化为当前会话可用的诊断证据

#### Scenario: 证据关联会话不存在
- **WHEN** 用户为不存在或已过期的会话上传证据
- **THEN** 系统 MUST 拒绝请求并返回未找到错误

### Requirement: 证据查询使用缓存
系统 SHALL 从缓存读取当前会话证据，作为诊断上下文输入。

#### Scenario: 诊断读取证据
- **WHEN** 用户触发诊断
- **THEN** 系统 MUST 读取当前会话缓存中的证据集合并构建证据链

