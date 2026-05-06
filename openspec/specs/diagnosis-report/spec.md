# diagnosis-report Specification

## Purpose
TBD - created by archiving change add-thread-doctor-mvp. Update Purpose after archive.
## Requirements
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

### Requirement: Cached diagnosis progress
The system SHALL maintain session-scoped diagnosis progress in application cache while a diagnosis report is being generated. The system MUST NOT persist diagnosis progress to a database or external storage.

#### Scenario: Diagnosis progress starts
- **WHEN** diagnosis starts for an incident session
- **THEN** the system MUST store progress for that session with a running status, a non-zero percent, a current step, and an updated timestamp

#### Scenario: Diagnosis progress advances through backend phases
- **WHEN** the backend completes diagnosis phases such as context building, rule detection, LLM generation, report validation, and report persistence
- **THEN** the system MUST update cached progress with the corresponding step and a monotonically non-decreasing percent

#### Scenario: Diagnosis progress completes
- **WHEN** the diagnosis report is generated and cached successfully
- **THEN** the system MUST update cached progress to completed status with percent equal to 100

#### Scenario: Diagnosis progress fails
- **WHEN** diagnosis fails before returning a report
- **THEN** the system MUST update cached progress to failed status with the last known step, percent, and an error message before propagating the error

### Requirement: Diagnosis progress query
The system SHALL expose a read API that returns the current diagnosis progress for an incident session.

#### Scenario: Query running progress
- **WHEN** a client queries progress for a session with an in-flight diagnosis
- **THEN** the system MUST return the cached status, percent, step, message, and timestamps for that session

#### Scenario: Query completed progress
- **WHEN** a client queries progress after diagnosis has completed and the progress cache entry has not expired
- **THEN** the system MUST return completed status with percent equal to 100

#### Scenario: Query missing progress
- **WHEN** a client queries progress for a session that has no diagnosis progress entry
- **THEN** the system MUST return a deterministic not-started response or a not-found error that the frontend can handle without failing the whole conversation

### Requirement: Template-backed diagnosis prompts
The system SHALL build diagnosis LLM prompts through the prompt template management layer.

#### Scenario: Build diagnosis prompt from templates
- **WHEN** a diagnosis report is requested for an incident session
- **THEN** the system loads the diagnosis system prompt, diagnosis user prompt template, and diagnosis JSON schema through typed prompt templates

#### Scenario: Render diagnosis prompt variables
- **WHEN** diagnosis prompt assembly runs
- **THEN** the user prompt template is rendered with `userGoal`, `incidentContext`, and `evidencePackJson` variables derived from the current diagnosis context

#### Scenario: Pass JSON schema to LLM client flow
- **WHEN** the diagnosis JSON schema template is loaded
- **THEN** the diagnosis flow uses that schema content as the expected structured response schema for the OpenAI-compatible client interaction

#### Scenario: Fail fast on invalid diagnosis prompt
- **WHEN** strict rendering is enabled and a diagnosis prompt template references a missing required variable
- **THEN** diagnosis generation fails before invoking the LLM client and returns a prompt-specific error

### Requirement: Diagnosis localization status
The system SHALL include explicit localization status and next-action metadata in diagnosis report responses.

#### Scenario: Return localized report metadata
- **WHEN** a diagnosis report has enough evidence to locate the problem
- **THEN** the report response MUST include `localizationStatus=LOCALIZED` and supporting evidence references

#### Scenario: Return unresolved report metadata
- **WHEN** a diagnosis report cannot locate the problem from supplied evidence
- **THEN** the report response MUST include `localizationStatus=UNRESOLVED`, unresolved reasons, and next-step options

#### Scenario: Return follow-up evidence metadata
- **WHEN** additional information is required before continuing diagnosis
- **THEN** the report response MUST include structured follow-up evidence requests that the frontend can render

#### Scenario: Return codebase prompt metadata
- **WHEN** a codebase investigation prompt is available
- **THEN** the report response MUST include the generated prompt and a document-only warning for Codex/OpenCode handoff

### Requirement: LLM diagnosis response validation
The system SHALL validate LLM diagnosis output before presenting it as a final diagnosis.

#### Scenario: Validate structured localization result
- **WHEN** the LLM client returns structured diagnosis content
- **THEN** the backend MUST validate localization status, confidence, evidence references, and next-step fields before caching the report

#### Scenario: Fallback for incomplete LLM output
- **WHEN** the LLM output omits required localization fields or cannot be parsed
- **THEN** the backend MUST create an unresolved diagnosis result with clear limitations instead of presenting an unsupported final conclusion

