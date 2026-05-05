# fault-pattern-detection Specification

## Purpose
TBD - created by archiving change add-thread-doctor-mvp. Update Purpose after archive.
## Requirements
### Requirement: Redis 连接池耗尽检测
系统 SHALL 基于 Redis 指标、日志和 jstack 阻塞证据检测 Redis 连接池耗尽。

#### Scenario: Redis 池满并有等待线程
- **WHEN** Redis 指标显示 `active == maxActive`、`idle == 0` 且 `waiters > 0`
- **THEN** 系统 MUST 输出 Redis 连接池耗尽候选故障模式，并引用相关证据 ID

### Requirement: 锁竞争检测
系统 SHALL 基于 jstack 锁等待信息检测明显锁竞争。

#### Scenario: 多线程等待同一锁
- **WHEN** jstack 分析发现多个线程等待同一锁对象
- **THEN** 系统 MUST 输出锁竞争候选故障模式和等待线程摘要

### Requirement: JVM 与 Kafka 常见模式检测
系统 SHALL 覆盖 Full GC 压力和 Kafka 积压 MVP 场景。

#### Scenario: Full GC 压力
- **WHEN** JVM 指标或日志显示 Full GC 频繁且耗时异常
- **THEN** 系统 MUST 输出 Full GC 压力候选故障模式

#### Scenario: Kafka 消费积压
- **WHEN** Kafka 指标显示消费 lag 持续偏高
- **THEN** 系统 MUST 输出 Kafka 积压候选故障模式

