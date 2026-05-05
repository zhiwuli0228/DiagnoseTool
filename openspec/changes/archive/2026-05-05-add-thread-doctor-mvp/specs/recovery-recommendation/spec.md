## ADDED Requirements

### Requirement: 缓存型恢复建议
系统 SHALL 基于诊断报告生成风险分级恢复建议，并 SHALL 将恢复动作缓存在当前会话中。系统 MUST NOT 将恢复动作写入数据库。

#### Scenario: 生成 Redis 恢复建议
- **WHEN** 诊断报告指出 Redis 连接池耗尽
- **THEN** 系统 MUST 生成包含风险等级、操作说明和验证方式的恢复建议

#### Scenario: 查询缓存恢复动作
- **WHEN** 用户查询当前会话已生成且未过期的恢复动作
- **THEN** 系统 MUST 从缓存返回恢复动作列表

### Requirement: 仅模拟执行
系统 SHALL 只支持模拟执行恢复动作，并 MUST 禁止真实生产变更。

#### Scenario: 模拟执行恢复动作
- **WHEN** 用户触发恢复动作执行
- **THEN** 系统 MUST 返回标记为 `SIMULATED` 的执行结果

#### Scenario: 高风险恢复建议
- **WHEN** 恢复建议风险等级为高
- **THEN** 系统 MUST 标记需要人工确认并不得执行真实变更
