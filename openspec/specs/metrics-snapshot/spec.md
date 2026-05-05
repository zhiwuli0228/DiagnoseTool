# metrics-snapshot Specification

## Purpose
TBD - created by archiving change add-thread-doctor-mvp. Update Purpose after archive.
## Requirements
### Requirement: 缓存型指标快照
系统 SHALL 支持为故障会话缓存 JVM、Redis、Kafka 和 DB 连接池指标快照。系统 MUST NOT 将指标快照写入数据库。

#### Scenario: 缓存 Redis 指标快照
- **WHEN** 用户提交包含 `maxActive`、`active`、`idle`、`waiters`、`borrowTimeoutCount` 和 `commandTimeoutCount` 的 Redis 指标
- **THEN** 系统 MUST 将该指标快照缓存在当前会话下

#### Scenario: 诊断使用 Redis 指标
- **WHEN** 会话缓存中存在 Redis 指标快照且触发诊断
- **THEN** 系统 MUST 将 Redis active、waiters 和超时计数提供给故障检测器

#### Scenario: 指标缓存缺失
- **WHEN** 诊断需要 Redis 指标但当前会话缓存未提供
- **THEN** 系统 MUST 在诊断上下文中表达缺失指标信息

