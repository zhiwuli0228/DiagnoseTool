新增 Thread Doctor 的日志智能扫描与故障上下文生成能力，作为 AI 辅助研发的前置增强器。该能力支持扫描本地日志目录，或分析用户上传的日志 zip 包；系统需要在大体积日志中完成日志解析、异常聚类、关键证据提取、故障时间线生成，并最终输出 Codex/OpenSpec/下游大模型可消费的结构化故障上下文包。

背景：
当前 AI 编程工具已经具备 codebase 能力，但真实故障处理中，研发仍需要手动检索海量日志、筛选关键异常、还原故障时间线、整理问题上下文，再交给 Codex 或下游大模型分析。问题不在于 AI 不会读代码，而在于输入给 AI 的上下文质量低、噪声高、不结构化。因此 Thread Doctor 不应重复做一个普通 AI 故障分析器，而应聚焦日志智能检索分析，把生产日志转化为高质量、结构化、可追溯的故障证据包。

目标：
1. 支持用户上传日志 zip 包进行分析，zip 包可能较大，不能一次性全部加载到内存。
2. 支持配置本地日志目录扫描，递归读取目录下的日志文件。
3. 支持常见文本日志格式解析，提取时间、级别、线程名、logger、traceId、message、异常类型、堆栈等字段。
4. 支持多行异常堆栈合并，避免将 Java stacktrace 拆成多条孤立日志。
5. 支持关键词、时间范围、日志级别、线程名、traceId、异常类型、类名、方法名等条件检索。
6. 支持异常日志聚类，降低重复异常日志噪声。
7. 支持生成故障时间线，按时间顺序还原关键异常演进过程。
8. 支持提取关键证据链，包括首个异常、高频异常、关键 traceId 日志、最长耗时日志、关键堆栈片段、疑似代码区域。
9. 支持生成 Evidence Pack，作为 Codex/OpenSpec/下游 LLM 的标准输入。
10. 支持生成 Codex codebase investigation task，用于引导 Codex 基于代码仓定向排查。
11. 支持生成 OpenSpec change proposal draft，用于将故障分析转化为后续代码整改需求。
12. 设计时需要考虑大日志包处理性能、内存控制、文件安全和可扩展性。

范围：
本 change 聚焦日志扫描、日志解析、日志聚类、证据包生成和下游任务生成，不要求接入真实生产日志平台，也不要求真实执行生产自愈动作。

需要实现的能力：

一、日志输入能力

1. LogSource 支持两类输入：
   - ZIP_UPLOAD：用户上传日志 zip 包；
   - DIRECTORY_SCAN：扫描本地日志目录。

2. ZIP_UPLOAD 要求：
   - 支持 .zip 文件；
   - 支持 zip 内多层目录；
   - 支持过滤非日志文件；
   - 支持大文件流式读取；
   - 禁止 zip slip 路径穿越风险；
   - 对单文件大小、总解压大小、文件数量设置保护阈值；
   - 不要求将 zip 全量解压到内存。

3. DIRECTORY_SCAN 要求：
   - 支持递归扫描；
   - 支持按文件后缀过滤，例如 .log、.txt、.out；
   - 支持忽略空文件和过大文件；
   - 支持扫描结果统计。

二、日志解析能力

1. 支持默认日志格式：
   2026-05-05 10:21:14.238 [cache-refresh-worker-3] ERROR c.example.cache.CacheRefreshService - Refresh cache failed, traceId=xxx

2. 需要解析字段：
   - timestamp
   - level
   - threadName
   - loggerName
   - traceId
   - message
   - exceptionType
   - stackTrace
   - rawText
   - sourceFile
   - lineNumber
   - tags

3. 支持 traceId 提取规则：
   - traceId=xxx
   - trace_id=xxx
   - traceId: xxx
   - [traceId:xxx]
   - X-B3-TraceId=xxx

4. 支持 Java 异常堆栈合并：
   - 以日志头识别新事件；
   - 后续以空白开头、at xxx、Caused by、Suppressed、... n common frames omitted 等行归并到上一条日志事件；
   - 合并后形成完整 LogEvent。

5. 无法识别时间戳的行：
   - 如果位于异常堆栈后续行，应合并到上一条事件；
   - 如果无法归属，应记录为 UNPARSED 事件，但不能中断整个分析流程。

三、日志检索能力

1. 支持 LogSearchRequest：
   - timeFrom
   - timeTo
   - levels
   - keywords
   - traceId
   - threadName
   - loggerName
   - exceptionType
   - sourceFile
   - limit
   - includeStackTrace

2. 支持关键词检索：
   - message 命中；
   - stackTrace 命中；
   - loggerName 命中；
   - exceptionType 命中。

3. 支持大小写不敏感检索。

4. 返回结果需要包含命中原因，例如命中了 message、stackTrace、exceptionType 或 loggerName。

四、异常聚类能力

1. 支持基于 fingerprint 的异常聚类。
2. fingerprint 生成逻辑：
   - 优先使用 exceptionType；
   - 结合业务堆栈 Top N 方法；
   - 对 message 中的变量进行归一化；
   - 数字、UUID、IP、traceId、耗时、cacheKey 等应替换为占位符。
3. LogCluster 字段：
   - clusterId
   - fingerprint
   - exceptionType
   - count
   - firstSeen
   - lastSeen
   - sampleEventIds
   - sampleLogs
   - threadNames
   - loggerNames
   - suspectedClasses
   - suspectedMethods
   - severity
4. 支持按 count、severity、firstSeen 排序。
5. 支持限制 sampleLogs 数量，避免输出过大。

五、故障时间线能力

1. 生成 IncidentTimeline。
2. 时间线事件来源：
   - ERROR / WARN 日志；
   - 首次出现的异常簇；
   - 高频异常簇；
   - 包含 timeout、failed、rejected、oom、deadlock、pool exhausted、connection refused 等关键词的日志；
   - 用户提供的关键词命中日志。
3. TimelineEvent 字段：
   - time
   - eventType
   - severity
   - summary
   - sourceFile
   - threadName
   - traceId
   - relatedClusterId
   - evidenceEventId
4. 时间线需要按时间排序。
5. 需要去重，避免同一异常刷屏导致时间线不可读。

六、关键证据链提取能力

1. EvidenceExtractor 需要提取：
   - 第一个 ERROR；
   - 第一个关键异常；
   - Top 高频异常簇；
   - 与指定 traceId 相关的完整日志链路；
   - 关键堆栈片段；
   - 可能指向业务代码的类名和方法名；
   - 可能的排除项，例如未观察到 GC、DB、Kafka 相关异常时应谨慎说明证据不足，而不是强行判断。
2. EvidenceItem 字段：
   - evidenceId
   - type
   - title
   - summary
   - confidence
   - sourceEventIds
   - sourceFile
   - rawExcerpt
   - relatedClasses
   - relatedMethods
3. rawExcerpt 需要限制最大长度，避免 Evidence Pack 过大。
4. EvidenceExtractor 不允许编造日志中不存在的信息。

七、疑似代码区域提取能力

1. 从 stackTrace 和 loggerName 中提取疑似代码区域。
2. 优先提取非 JDK、非第三方库的业务类。
3. 需要排除常见包：
   - java.
   - javax.
   - jakarta.
   - sun.
   - jdk.
   - org.springframework.
   - redis.clients.
   - org.apache.
   - com.zaxxer.
   - org.slf4j.
4. 输出：
   - suspectedClasses
   - suspectedMethods
   - reason
   - relatedEvidenceIds

八、Evidence Pack 生成能力

1. 新增 EvidencePackBuilder。
2. EvidencePack 需要包含：
   - sessionId
   - sourceSummary
   - logFileSummary
   - incidentSummary
   - keyClusters
   - timeline
   - evidenceItems
   - suspectedCodeAreas
   - recommendedCodexQuestions
   - recommendedChecks
   - limitations
3. Evidence Pack 用于下游大模型和 Codex，不应包含海量原始日志。
4. Evidence Pack 必须可序列化为 JSON 和 Markdown。

九、Codex Task 生成能力

1. 新增 CodexTaskGenerator。
2. 根据 Evidence Pack 生成 Markdown 格式的 Codex codebase investigation task。
3. 输出结构：
   - Title
   - Incident Summary
   - Key Evidence
   - Timeline
   - Suspected Code Areas
   - Questions to Answer
   - Required Codebase Investigation
   - Required Changes if Root Cause Confirmed
   - Tests to Add
   - Engineering Constraints
   - Do Not
4. 工程约束中需要包含：
   - JDK 21；
   - Maven；
   - JUnit5 + Mockito；
   - 不使用 PowerMock；
   - 代码命名使用英文；
   - 注释尽量使用中文；
   - 修改后运行 mvn test。
5. Codex Task 不应要求 Codex 编造不存在的代码路径，只能基于日志中提取的疑似类名和方法名提示其去代码仓检索确认。

十、OpenSpec Change Draft 生成能力

1. 新增 OpenSpecChangeDraftGenerator。
2. 根据 Evidence Pack 生成 OpenSpec change 草案。
3. 输出内容：
   - proposal.md
   - tasks.md
   - specs draft
4. proposal.md 需要包含：
   - Why
   - What Changes
   - Impact
   - Risk
   - Rollback
5. tasks.md 需要包含：
   - 代码走读确认根因；
   - 修复方案设计；
   - 单元测试；
   - 回归测试；
   - 风险验证；
   - 文档更新。
6. specs draft 可以是建议性草案，不要求直接写入 openspec 目录。

十一、API 设计

如果项目已有 REST API 风格，请按既有风格实现。建议接口如下：

1. 创建日志分析会话：
   POST /api/log-analysis/sessions

2. 上传 zip 包并开始分析：
   POST /api/log-analysis/sessions/{sessionId}/zip

3. 扫描本地日志目录：
   POST /api/log-analysis/sessions/{sessionId}/directory-scan

4. 查询分析状态：
   GET /api/log-analysis/sessions/{sessionId}

5. 检索日志：
   POST /api/log-analysis/sessions/{sessionId}/search

6. 获取异常聚类：
   GET /api/log-analysis/sessions/{sessionId}/clusters

7. 获取故障时间线：
   GET /api/log-analysis/sessions/{sessionId}/timeline

8. 获取 Evidence Pack：
   GET /api/log-analysis/sessions/{sessionId}/evidence-pack

9. 生成 Codex Task：
   POST /api/log-analysis/sessions/{sessionId}/codex-task

10. 生成 OpenSpec Change Draft：
    POST /api/log-analysis/sessions/{sessionId}/openspec-change-draft

十二、核心领域模型建议

需要至少包含以下模型：
- LogAnalysisSession
- LogSource
- LogSourceType
- LogFileSummary
- LogEvent
- LogSearchRequest
- LogSearchResult
- LogCluster
- IncidentTimeline
- TimelineEvent
- EvidencePack
- EvidenceItem
- SuspectedCodeArea
- CodexTask
- OpenSpecChangeDraft

十三、性能与安全要求

1. 大 zip 包不能一次性读入内存。
2. 单条 LogEvent 的 rawText 和 stackTrace 要有最大长度限制。
3. 分析结果要支持分页或限制输出数量。
4. zip 文件必须防止路径穿越。
5. zip 解压或读取要限制：
   - 最大文件数量；
   - 最大单文件大小；
   - 最大总读取字节数；
   - 最大目录层级。
6. 日志内容可能包含敏感信息，生成 Codex Task 和 Evidence Pack 时需要预留脱敏扩展点。
7. 脱敏接口可以先实现基础能力：
   - IP 脱敏；
   - token/password/secret/key 等字段脱敏；
   - 手机号、邮箱脱敏。
8. 分析失败不能导致整个服务崩溃，需要记录失败文件和失败原因。

十四、测试要求

必须新增单元测试，覆盖以下场景：
1. zip 包内多层目录日志读取；
2. zip slip 路径穿越防护；
3. 大日志文件流式读取，不一次性加载；
4. 标准单行日志解析；
5. Java 多行异常堆栈合并；
6. traceId 提取；
7. 关键词检索；
8. 异常 fingerprint 生成；
9. 相似异常聚类；
10. 时间线生成和去重；
11. Evidence Pack 生成；
12. Codex Task Markdown 生成；
13. OpenSpec Change Draft 生成；
14. 敏感字段脱敏；
15. 无法解析日志行的容错处理。

十五、验收标准

1. 用户可以上传包含多个日志文件的 zip 包并完成分析。
2. 系统可以输出日志文件统计、日志事件数量、错误数量、异常簇数量。
3. 系统可以正确合并 Java 多行异常堆栈。
4. 系统可以基于异常类型和业务栈生成聚类结果。
5. 系统可以生成按时间排序的故障时间线。
6. 系统可以生成 Evidence Pack JSON 和 Markdown。
7. 系统可以生成 Codex 可读的 codebase investigation task。
8. 系统可以生成 OpenSpec change draft。
9. 大文件处理过程中不应出现明显内存暴涨。
10. 所有新增核心逻辑必须有 JUnit5 单元测试。
11. 执行 mvn test 必须通过。

十六、非目标

本 change 不要求：
1. 接入真实生产日志平台；
2. 接入 Elasticsearch、Loki、Splunk 等外部系统；
3. 执行真实生产自愈动作；
4. 自动修改代码；
5. 自动提交 OpenSpec change 到仓库；
6. 训练或微调模型；
7. 实现复杂前端页面。

请基于以上内容创建 OpenSpec change，要求：
1. 生成清晰的 proposal.md；
2. 生成可执行的 tasks.md；
3. 如果需要新增或修改 specs，请按 OpenSpec 规范编写 delta spec；
4. change id 建议使用 add-log-intelligence-evidence-pack；
5. 确保 change 范围收敛，避免一次性扩展到生产自愈、代码自动修改或外部日志平台集成。