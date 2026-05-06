将已创建的 Thread Doctor 诊断 Prompt 模板集成到项目中，形成可配置、可加载、可渲染、可测试的 Prompt 管理能力，并让日志 Evidence Pack、诊断报告生成、Codex Task 生成、OpenSpec Change Draft 生成流程统一使用这些模板。

背景：
当前项目已经具备日志扫描、日志解析、异常聚类、时间线生成、Evidence Pack 生成，以及面向 Codex/OpenSpec/下游大模型的上下文生成能力。为了提升大模型诊断质量，需要将诊断 Prompt 从硬编码逻辑中抽离，统一放到项目模板目录中管理。用户已经手动创建了相关模板文件，例如：
- prompts/diagnosis/system-prompt.md
- prompts/diagnosis/user-prompt-template.md
- prompts/diagnosis/json-schema.json
- prompts/diagnosis/incident-diagnosis-context.md
- prompts/codex-task/codex-investigation-task-template.md
- prompts/openspec/openspec-change-draft-template.md
- prompts/review/incident-review-template.md

本 change 的目标不是重新设计诊断逻辑，而是将这些模板集成进现有项目，让业务流程可以按模板渲染 Prompt，并支持后续扩展和测试。

目标：
1. 新增 Prompt 模板加载能力。
2. 新增 Prompt 模板渲染能力。
3. 支持从 classpath 或项目配置目录读取 prompt 模板。
4. 支持 Markdown 模板和 JSON Schema 模板。
5. 将诊断流程接入 diagnosis/system-prompt.md 和 diagnosis/user-prompt-template.md。
6. 将结构化输出流程接入 diagnosis/json-schema.json。
7. 将 Codex Task 生成流程接入 codex-task/codex-investigation-task-template.md。
8. 将 OpenSpec Change Draft 生成流程接入 openspec/openspec-change-draft-template.md。
9. 将故障复盘草案生成流程接入 review/incident-review-template.md。
10. 提供模板变量校验能力，避免模板变量缺失导致生成错误 Prompt。
11. 提供单元测试，覆盖模板加载、变量渲染、缺失变量处理和业务流程集成。

范围：
本 change 只负责 prompt 模板工程化集成，不要求修改日志解析、异常聚类、Evidence Pack 生成的核心算法，不要求接入真实大模型供应商，不要求重新设计前端页面。

需要实现的能力：

一、Prompt 模板目录规范

1. 项目应支持以下模板路径：
   - prompts/diagnosis/system-prompt.md
   - prompts/diagnosis/user-prompt-template.md
   - prompts/diagnosis/json-schema.json
   - prompts/codex-task/codex-investigation-task-template.md
   - prompts/openspec/openspec-change-draft-template.md
   - prompts/review/incident-review-template.md

2. 如果项目使用 Maven，建议将模板文件放入：
   - src/main/resources/prompts/...

3. 如果当前项目已经有独立配置目录，也可以支持从外部目录加载，例如：
   - ${thread-doctor.prompt.template-dir}

4. 加载优先级建议：
   - 外部配置目录优先；
   - classpath resources 兜底。

二、Prompt 模板枚举

新增 PromptTemplateType 或类似枚举，至少包含：

- DIAGNOSIS_SYSTEM_PROMPT
- DIAGNOSIS_USER_PROMPT
- DIAGNOSIS_JSON_SCHEMA
- CODEX_INVESTIGATION_TASK
- OPENSPEC_CHANGE_DRAFT
- INCIDENT_REVIEW

每个枚举需要维护：
- templateId
- defaultPath
- contentType，例如 MARKDOWN 或 JSON
- description

三、Prompt 模板加载器

新增 PromptTemplateLoader 或类似组件。

能力要求：
1. 根据 PromptTemplateType 加载模板内容。
2. 支持从 classpath 加载。
3. 支持从外部目录加载。
4. 如果外部目录存在同名模板，优先使用外部模板。
5. 如果模板不存在，需要抛出明确异常。
6. 不允许静默返回空字符串。
7. 支持缓存模板内容，避免每次调用都读取文件。
8. 支持刷新缓存的方法，方便后续调试或热更新扩展。
9. 单元测试需要覆盖：
   - classpath 模板加载；
   - 外部目录模板覆盖；
   - 模板不存在；
   - 缓存命中；
   - 缓存刷新。

四、Prompt 模板渲染器

新增 PromptRenderer 或类似组件。

能力要求：
1. 支持使用变量 Map 渲染模板。
2. 变量格式使用 {{variableName}}。
3. 支持变量名包含字母、数字、下划线、点号，例如 {{incident.sessionId}}。
4. 如果变量缺失，默认应抛出明确异常，避免生成残缺 Prompt。
5. 可提供 relaxed 模式，允许缺失变量保留原样，但默认不启用。
6. 支持将对象序列化为 JSON 字符串后注入模板，例如 {{evidencePackJson}}。
7. 支持基础转义策略：
   - Markdown 模板按原文注入；
   - JSON Schema 模板不做变量替换，除非明确需要。
8. 单元测试需要覆盖：
   - 正常变量替换；
   - 重复变量替换；
   - 缺失变量报错；
   - 点号变量名；
   - JSON 字符串注入；
   - relaxed 模式。

五、Prompt 组装服务

新增 PromptAssemblyService 或类似服务。

能力要求：
1. buildDiagnosisPrompt(EvidencePack evidencePack, DiagnosisRequest request)
   - 加载 DIAGNOSIS_SYSTEM_PROMPT；
   - 加载 DIAGNOSIS_USER_PROMPT；
   - 将 userGoal、incidentContext、evidencePackJson 渲染到 user prompt；
   - 返回包含 systemPrompt、userPrompt、jsonSchema 的对象。

2. buildCodexTaskPrompt(EvidencePack evidencePack)
   - 加载 CODEX_INVESTIGATION_TASK 模板；
   - 渲染 incidentSummary、keyEvidence、timeline、suspectedCodeAreas、recommendedChecks 等变量；
   - 返回 Markdown。

3. buildOpenSpecChangeDraftPrompt(EvidencePack evidencePack)
   - 加载 OPENSPEC_CHANGE_DRAFT 模板；
   - 渲染故障背景、证据链、影响面、验收标准建议等变量；
   - 返回 Markdown。

4. buildIncidentReviewPrompt(EvidencePack evidencePack, DiagnosisReport diagnosisReport)
   - 加载 INCIDENT_REVIEW 模板；
   - 渲染故障摘要、根因候选、证据链、处置建议、长期整改建议；
   - 返回 Markdown。

5. 如果当前项目已经存在 CodexTaskGenerator、OpenSpecChangeDraftGenerator、DiagnosisPromptBuilder 等组件，应优先复用并重构，不要重复实现平行逻辑。

六、诊断流程集成

1. 现有诊断报告生成逻辑应改为使用模板生成 prompt。
2. 不应在 Java 代码中硬编码大段 system prompt 或 user prompt。
3. Prompt 模板中需要注入的字段至少包括：
   - userGoal
   - incidentContext
   - evidencePackJson
4. diagnosis/json-schema.json 用于结构化输出场景。
5. 如果当前 LLM Client 还不支持 response schema，可以先将 json-schema.json 加载并封装到 DiagnosisPrompt 对象中，供后续调用层使用。
6. 保留无大模型环境下的单元测试能力，可以使用 Mock LLM Client。

七、Codex Task 生成流程集成

1. CodexTaskGenerator 应使用 prompts/codex-task/codex-investigation-task-template.md。
2. 不应再硬编码完整 Markdown 模板。
3. 输出中必须保留工程约束：
   - JDK 21
   - Maven
   - JUnit5 + Mockito
   - Do not use PowerMock
   - Code identifiers in English
   - Comments in Chinese where necessary
   - Run mvn test
4. 输出中必须强调：
   - Codex 需要基于代码仓确认日志中出现的类和方法是否真实存在；
   - 不允许基于日志直接编造代码实现；
   - 如果确认根因，需要补充单元测试或回归测试。

八、OpenSpec Change Draft 生成流程集成

1. OpenSpecChangeDraftGenerator 应使用 prompts/openspec/openspec-change-draft-template.md。
2. 输出至少包含：
   - Change Title
   - Why
   - What Changes
   - Impact
   - Risk
   - Rollback
   - Acceptance Criteria
   - Tests
3. 输出需要明确这是 draft，不直接写入 openspec/changes 目录，除非后续用户确认。
4. 不允许在证据不足时生成过度确定的整改要求。

九、Incident Review 生成流程集成

1. IncidentReviewGenerator 或类似组件应使用 prompts/review/incident-review-template.md。
2. 输出用于故障复盘草案。
3. 至少包含：
   - 故障摘要
   - 时间线
   - 影响范围
   - 根因候选
   - 关键证据
   - 临时处置
   - 长期整改
   - 待补充信息
   - 经验沉淀标签

十、配置项建议

新增配置项：

- thread-doctor.prompt.template-dir
- thread-doctor.prompt.cache-enabled
- thread-doctor.prompt.strict-rendering
- thread-doctor.prompt.default-output-language

默认值建议：
- template-dir 为空，表示使用 classpath resources；
- cache-enabled=true；
- strict-rendering=true；
- default-output-language=zh-CN。

如果项目当前已有配置体系，请接入既有配置体系。

十一、核心领域模型建议

新增或复用以下模型：

1. PromptTemplate
   - templateType
   - path
   - content
   - contentType
   - loadedFrom
   - loadedAt

2. PromptRenderRequest
   - templateType
   - variables
   - strict

3. PromptRenderResult
   - templateType
   - renderedContent
   - unresolvedVariables
   - renderedAt

4. DiagnosisPrompt
   - systemPrompt
   - userPrompt
   - jsonSchema

5. PromptContentType
   - MARKDOWN
   - JSON
   - TEXT

6. PromptTemplateSource
   - CLASSPATH
   - EXTERNAL_FILE

十二、异常处理要求

新增清晰异常类型，或复用项目既有业务异常：

- PromptTemplateNotFoundException
- PromptTemplateLoadException
- PromptRenderException
- MissingPromptVariableException

异常信息中需要包含：
- templateType
- templatePath
- missingVariables，如存在
- root cause message

十三、测试要求

必须新增单元测试，覆盖：

1. PromptTemplateType 默认路径正确。
2. classpath 模板可加载。
3. 外部目录模板可以覆盖 classpath 模板。
4. 模板不存在时抛出明确异常。
5. 模板缓存生效。
6. 刷新缓存后可以重新加载模板。
7. PromptRenderer 可以替换普通变量。
8. PromptRenderer 可以替换重复变量。
9. PromptRenderer 支持点号变量名。
10. PromptRenderer 缺失变量时 strict 模式报错。
11. PromptRenderer relaxed 模式保留未解析变量。
12. EvidencePack 可以被序列化为 evidencePackJson 并注入 user-prompt-template.md。
13. DiagnosisPrompt 组装结果包含 systemPrompt、userPrompt、jsonSchema。
14. CodexTaskGenerator 使用模板渲染而不是硬编码。
15. OpenSpecChangeDraftGenerator 使用模板渲染而不是硬编码。
16. IncidentReviewGenerator 使用模板渲染而不是硬编码。
17. 执行 mvn test 必须通过。

十四、验收标准

1. 项目可以从 src/main/resources/prompts 加载所有默认 prompt 模板。
2. 项目可以通过配置项指定外部 prompt 模板目录，并优先使用外部模板。
3. 诊断流程可以生成包含 systemPrompt、userPrompt、jsonSchema 的 DiagnosisPrompt。
4. userPrompt 中能正确注入 userGoal、incidentContext、evidencePackJson。
5. Codex Task 生成使用 codex-investigation-task-template.md。
6. OpenSpec Change Draft 生成使用 openspec-change-draft-template.md。
7. Incident Review 生成使用 incident-review-template.md。
8. 模板变量缺失时有明确错误，而不是输出残缺 Prompt。
9. 大段 Prompt 不再硬编码在 Java 业务代码中。
10. 所有新增核心逻辑具备 JUnit5 单元测试。
11. mvn test 通过。

十五、非目标

本 change 不要求：
1. 重新设计日志扫描和 Evidence Pack 算法；
2. 接入真实 LLM 供应商；
3. 实现前端 Prompt 编辑页面；
4. 实现 Prompt 热更新监听；
5. 实现复杂模板语法，例如条件判断和循环；
6. 自动提交 OpenSpec change；
7. 自动让 Codex 修改代码；
8. 训练或微调模型。

请基于以上内容创建 OpenSpec change，要求：
1. change id 建议使用 integrate-prompt-template-management；
2. 生成清晰的 proposal.md；
3. 生成可执行的 tasks.md；
4. 如需新增或修改 specs，请按 OpenSpec 规范编写 delta spec；
5. 保持范围收敛，只做 Prompt 模板集成与渲染，不扩展到日志分析算法或真实 LLM 接入。