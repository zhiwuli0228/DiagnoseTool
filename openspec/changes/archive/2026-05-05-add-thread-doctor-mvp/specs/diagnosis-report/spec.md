## ADDED Requirements

### Requirement: 缓存型诊断报告
系统 SHALL 结合规则检测、证据链和 LLM 推理生成结构化诊断报告，并 SHALL 将报告缓存在当前会话中。系统 MUST NOT 将诊断报告写入数据库。

#### Scenario: 生成 Redis 故障诊断报告
- **WHEN** 规则检测命中 Redis 连接池耗尽并存在相关证据
- **THEN** 系统 MUST 生成包含 Redis 候选根因和对应证据 ID 的诊断报告

#### Scenario: 查询缓存报告
- **WHEN** 用户查询当前会话已生成且未过期的诊断报告
- **THEN** 系统 MUST 从缓存返回报告详情

### Requirement: LLM 输出校验
系统 SHALL 校验 LLM 返回的 JSON 结构、枚举值和证据引用后再返回或缓存。

#### Scenario: LLM 返回无效 JSON
- **WHEN** LLM 返回无法解析或不符合 schema 的 JSON
- **THEN** 系统 MUST 拒绝缓存该输出，并返回可诊断的错误或降级结果

#### Scenario: 证据不足
- **WHEN** 诊断需要 Redis 指标但会话未提供 Redis 指标
- **THEN** 系统 MUST 在报告中列出缺失的 Redis 指标信息
