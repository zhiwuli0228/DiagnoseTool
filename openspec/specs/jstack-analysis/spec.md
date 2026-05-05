# jstack-analysis Specification

## Purpose
TBD - created by archiving change add-thread-doctor-mvp. Update Purpose after archive.
## Requirements
### Requirement: jstack 线程分析
系统 SHALL 解析 jstack 文本并输出线程状态、线程名称、tid、nid 和堆栈帧。

#### Scenario: 解析标准 jstack
- **WHEN** 用户上传标准 HotSpot jstack 文本
- **THEN** 系统 MUST 输出结构化线程列表和线程状态统计

### Requirement: 锁竞争与阻塞识别
系统 SHALL 识别死锁、锁等待、热点堆栈以及 Redis、JDBC、Kafka、HTTP、文件 IO 等常见阻塞关键词。

#### Scenario: 识别 Redis IO 阻塞
- **WHEN** jstack 堆栈包含 `RedisInputStream.ensureFill` 或 Jedis 协议读取调用
- **THEN** 系统 MUST 将相关线程标记为 Redis IO 阻塞可疑线程

#### Scenario: 聚合同锁等待线程
- **WHEN** 多个线程等待同一个锁对象
- **THEN** 系统 MUST 输出该锁对象和等待线程数量

