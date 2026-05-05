# incident-card-memory Specification

## Purpose
TBD - created by archiving change add-thread-doctor-mvp. Update Purpose after archive.
## Requirements
### Requirement: 当前会话结果文档
系统 SHALL 基于当前会话的诊断报告和恢复建议生成 Markdown 结果文档。系统 MUST NOT 将结果文档写入数据库或长期知识库。

#### Scenario: 生成结果文档
- **WHEN** 用户在前台完成报告和恢复建议查看后点击生成结果文档
- **THEN** 系统 MUST 返回包含故障摘要、证据链、候选根因、恢复建议、验证方式和预防建议的 Markdown 文档

#### Scenario: 读取当前会话结果文档
- **WHEN** 用户查看当前会话已生成且未过期的结果文档
- **THEN** 系统 MUST 从缓存返回该 Markdown 文档

#### Scenario: 结果文档缓存未命中
- **WHEN** 用户查询不存在或已过期的结果文档
- **THEN** 系统 MUST 返回可诊断的未找到错误

### Requirement: 前台输出结果文档
前端 SHALL 在用户触发后展示结果文档，并 SHALL 提供复制或下载当前 Markdown 内容的入口。

#### Scenario: 前台展示 Markdown 文档
- **WHEN** 后端返回结果文档 Markdown
- **THEN** 前端 MUST 展示文档内容并保持与当前会话关联

#### Scenario: 用户导出结果文档
- **WHEN** 用户点击复制或下载结果文档
- **THEN** 前端 MUST 输出当前会话的 Markdown 文档内容

