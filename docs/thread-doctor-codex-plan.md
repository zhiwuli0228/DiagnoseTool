# Thread Doctor Codex Implementation Plan

> 项目名称：Thread Doctor：面向 Java 生产故障的 AI 诊断与自愈智能体  
> 目标：使用 Codex + OpenSpec 逐步实现一个可演示、可扩展、可测试的 Java 生产故障诊断智能体系统。

---

## 1. Project Overview

Thread Doctor is an AI-assisted diagnosis and recovery system for Java production incidents.

It focuses on diagnosing common Java backend failures by combining:

- log snippets
- jstack thread dumps
- JVM metrics
- Redis metrics
- Kafka metrics
- DB connection pool metrics
- historical incident cases
- predefined fault patterns
- LLM-based reasoning

The system should produce:

- structured diagnosis reports
- candidate root causes
- evidence chains
- risk-classified recovery suggestions
- simulated recovery actions for MVP
- incident review cards for knowledge retention

---

## 2. Core Product Positioning

Thread Doctor is not a generic chatbot.

It is a production incident diagnosis agent with the following workflow:

```text
Incident input
  -> Evidence collection
  -> Structured parsing
  -> Fault pattern detection
  -> AI-assisted reasoning
  -> Diagnosis report generation
  -> Recovery suggestion generation
  -> Recovery verification
  -> Incident card generation
```

The MVP should prioritize deterministic analysis and structured outputs. LLM should be used for reasoning, summarization, and report generation, not for replacing all rule-based diagnosis logic.

---

## 3. Global Development Rules

### 3.1 Java and Build Rules

- Use JDK 21.
- Use Maven.
- All generated business code must have corresponding unit tests.
- Unit tests must use JUnit 5 and Mockito.
- Do not use PowerMock unless explicitly requested.
- Code identifiers must be in English.
- Comments should be written in Chinese where explanation is needed.
- Avoid over-engineering in MVP.
- Prefer small, testable services over large utility classes.

### 3.2 AI Integration Rules

- Business code must not directly depend on a specific LLM provider SDK.
- All LLM calls must go through an abstraction named `LlmClient`.
- LLM output must be parsed and validated before being persisted or returned.
- Prompt templates should be centrally managed.
- Diagnosis conclusions must not rely only on LLM free text.
- Every root cause candidate must be linked to evidence.
- If evidence is insufficient, the system must explicitly state missing information.

### 3.3 Safety Rules

- MVP must not execute high-risk production operations.
- Recovery actions should default to simulated execution.
- High-risk actions such as restart, traffic switching, or data repair must only generate recommendations.
- Any action beyond read-only must be modeled with risk level and manual confirmation.
- Never suggest deleting or modifying production data without explicit safety controls.

### 3.4 Testing Rules

- Every parser must have sample-based unit tests.
- Every fault detector must have positive and negative test cases.
- LLM-related tests should use mock implementations.
- Tests must not require external network access.
- Tests must be executable with:

```bash
mvn test
```

---

## 4. Recommended Technology Stack

### 4.1 MVP Stack

Use this stack if the goal is fast competition delivery:

- Java 21
- Spring Boot
- Maven
- H2 or SQLite for local persistence
- Caffeine for local cache if needed
- OpenAI-compatible LLM client abstraction
- JUnit 5 + Mockito
- OpenSpec for requirement/change management
- Codex for implementation

### 4.2 Production-Oriented Stack

Use this stack if integrating with an existing Java service:

- Java 21 or project-compatible Java version
- Spring or existing project framework
- Maven
- MySQL
- Caffeine
- Redis/Kafka/JVM metric adapters
- Internal LLM gateway through `LlmClient`

---

## 5. System Architecture

```text
┌──────────────────────────────────────────────┐
│                  User Interface               │
│  Web UI / REST API / CLI / ChatOps / Alert In │
└──────────────────────┬───────────────────────┘
                       │
┌──────────────────────▼───────────────────────┐
│              Agent Orchestrator               │
│  planning / tool calling / evidence assembly  │
└──────────────────────┬───────────────────────┘
                       │
┌──────────────────────▼───────────────────────┐
│               Diagnosis Engine                │
│  rule detection / root cause scoring / report │
└──────────────────────┬───────────────────────┘
                       │
┌──────────────────────▼───────────────────────┐
│                 Tool Runtime                  │
│  log / jstack / JVM / Redis / Kafka / DB tools │
└──────────────────────┬───────────────────────┘
                       │
┌──────────────────────▼───────────────────────┐
│               Knowledge Layer                 │
│  fault patterns / SOP / historical incidents  │
└──────────────────────┬───────────────────────┘
                       │
┌──────────────────────▼───────────────────────┐
│              Recovery Engine                  │
│  recommendation / simulation / verification   │
└──────────────────────────────────────────────┘
```

---

## 6. Module Design

### 6.1 `incident-session`

Responsible for incident lifecycle management.

#### Responsibilities

- Create incident session.
- Store incident title, description, severity, and status.
- Associate evidence, diagnosis reports, recovery actions, and incident cards.
- Provide query APIs.

#### Core Model

```java
public class IncidentSession {
    private String sessionId;
    private String title;
    private String description;
    private IncidentStatus status;
    private SeverityLevel severity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### Status Flow

```text
CREATED
  -> COLLECTING_EVIDENCE
  -> DIAGNOSING
  -> WAITING_CONFIRMATION
  -> RECOVERING
  -> VERIFYING
  -> RESOLVED / FAILED
```

---

### 6.2 `evidence-collector`

Responsible for evidence ingestion and normalization.

#### Evidence Types

```java
public enum EvidenceType {
    ALERT_TEXT,
    LOG_SNIPPET,
    JSTACK,
    JVM_METRICS,
    REDIS_METRICS,
    KAFKA_METRICS,
    DB_METRICS,
    CONFIG_CHANGE,
    MANUAL_NOTE
}
```

#### Core Model

```java
public class Evidence {
    private String evidenceId;
    private String sessionId;
    private EvidenceType type;
    private String source;
    private String content;
    private String parsedSummary;
    private LocalDateTime collectedAt;
    private Map<String, Object> metadata;
}
```

---

### 6.3 `jstack-analyzer`

This is one of the most important technical modules.

#### Responsibilities

- Parse jstack text.
- Count thread states.
- Detect Java deadlocks.
- Detect lock contention.
- Identify hot stack traces.
- Identify Redis/Kafka/DB/HTTP/File IO blocking patterns.
- Group threads by name prefix.

#### Analysis Items

| Item | Description |
|---|---|
| total threads | Count all parsed threads |
| thread state distribution | RUNNABLE / WAITING / BLOCKED / TIMED_WAITING |
| deadlock detection | Detect deadlock text in jstack |
| lock contention | Aggregate waiting threads by same lock object |
| hot stack | Group similar stack traces |
| IO blocking | Detect socketRead, RedisInputStream, JDBC, Kafka poll, etc. |
| thread pool grouping | Group by thread name prefix |

#### Core Model

```java
public class JstackAnalysisResult {
    private int totalThreads;
    private Map<ThreadState, Integer> stateCount;
    private List<ThreadGroupSummary> threadGroups;
    private List<LockContention> lockContentions;
    private List<HotStack> hotStacks;
    private List<SuspiciousThread> suspiciousThreads;
    private boolean deadlockDetected;
    private List<String> deadlockDetails;
}
```

#### Parser Flow

```text
1. Split jstack into thread blocks.
2. Extract thread name, tid, nid, and thread state.
3. Extract waiting on / parking to wait for / locked information.
4. Aggregate threads waiting for the same lock object.
5. Group similar stack traces by top N frames.
6. Match known blocking keywords.
7. Return structured analysis result.
```

---

### 6.4 `metrics-snapshot`

Responsible for storing and normalizing metric snapshots.

#### Core Model

```java
public class MetricsSnapshot {
    private String snapshotId;
    private String sessionId;
    private LocalDateTime timestamp;
    private JvmMetrics jvmMetrics;
    private RedisMetrics redisMetrics;
    private KafkaMetrics kafkaMetrics;
    private DbMetrics dbMetrics;
}
```

#### Redis Metrics

```java
public class RedisMetrics {
    private int maxActive;
    private int active;
    private int idle;
    private int waiters;
    private long borrowTimeoutCount;
    private long commandTimeoutCount;
}
```

#### Kafka Metrics

```java
public class KafkaMetrics {
    private String topic;
    private String groupId;
    private long totalLag;
    private double consumeRate;
    private long rebalanceCount;
}
```

#### JVM Metrics

```java
public class JvmMetrics {
    private long heapUsed;
    private long heapMax;
    private long oldGenUsed;
    private long oldGenMax;
    private int liveThreads;
    private long youngGcCount;
    private long fullGcCount;
    private double processCpuLoad;
}
```

---

### 6.5 `fault-pattern`

Responsible for deterministic fault pattern detection.

#### Fault Pattern Model

```java
public class FaultPattern {
    private String patternId;
    private String name;
    private String description;
    private List<String> symptoms;
    private List<String> evidenceRules;
    private List<String> possibleCauses;
    private List<String> recommendedActions;
    private SeverityLevel defaultSeverity;
}
```

#### Predefined Fault Patterns

| Pattern ID | Name |
|---|---|
| JAVA_DEADLOCK | Java deadlock |
| LOCK_CONTENTION | Lock contention |
| THREAD_POOL_EXHAUSTED | Thread pool exhausted |
| REDIS_POOL_EXHAUSTED | Redis pool exhausted |
| REDIS_IO_BLOCKED | Redis IO blocked |
| KAFKA_LAG_INCREASED | Kafka lag increased |
| DB_POOL_EXHAUSTED | DB pool exhausted |
| FULL_GC_PRESSURE | Full GC / memory pressure |
| SCHEDULE_TASK_STUCK | Scheduled task stuck |
| CACHE_INCONSISTENCY | Cache inconsistency |

#### Detector Interface

```java
public interface FaultPatternDetector {
    FaultPatternType type();

    DetectionResult detect(DiagnosisContext context);
}
```

---

### 6.6 `diagnosis-engine`

Responsible for generating final diagnosis results.

#### Diagnosis Flow

```text
Structured evidence
  -> Rule-based fault detection
  -> Candidate root cause ranking
  -> Historical case retrieval
  -> LLM reasoning
  -> JSON output validation
  -> Diagnosis report persistence
```

#### Diagnosis Report Model

```java
public class DiagnosisReport {
    private String reportId;
    private String sessionId;
    private String summary;
    private List<CandidateRootCause> candidateRootCauses;
    private List<EvidenceChain> evidenceChains;
    private List<RecommendedAction> recommendedActions;
    private ConfidenceLevel confidence;
    private LocalDateTime generatedAt;
}
```

#### Candidate Root Cause Model

```java
public class CandidateRootCause {
    private String causeId;
    private String title;
    private String description;
    private double score;
    private List<String> supportingEvidenceIds;
    private List<String> riskNotes;
}
```

---

### 6.7 `agent-orchestrator`

Responsible for planning and coordinating diagnosis steps.

#### Responsibilities

- Determine whether evidence is sufficient.
- Generate evidence collection plan.
- Call diagnostic tools.
- Assemble diagnosis context.
- Trigger diagnosis report generation.
- Trigger recovery suggestion generation.
- Trigger incident card generation.

#### Agent Plan Model

```java
public class AgentPlan {
    private String planId;
    private String sessionId;
    private List<AgentStep> steps;
}
```

#### Agent Step Model

```java
public class AgentStep {
    private String stepId;
    private String name;
    private ToolType toolType;
    private Map<String, Object> input;
    private AgentStepStatus status;
    private String output;
}
```

---

### 6.8 `tool-runtime`

Responsible for executing diagnostic tools.

#### Tool Types

```java
public enum ToolType {
    LOG_SEARCH,
    JSTACK_ANALYZE,
    JVM_METRICS_QUERY,
    REDIS_METRICS_QUERY,
    KAFKA_LAG_QUERY,
    DB_POOL_QUERY,
    CONFIG_CHANGE_QUERY,
    RECOVERY_ACTION_EXECUTE,
    RECOVERY_VERIFY
}
```

#### Tool Interface

```java
public interface DiagnosticTool<I, O> {
    ToolType type();

    O execute(I input);

    boolean isReadOnly();

    RiskLevel riskLevel();
}
```

---

### 6.9 `recovery-engine`

Responsible for risk-classified recovery recommendations and simulated execution.

#### Risk Levels

```java
public enum RiskLevel {
    READ_ONLY,
    LOW_RISK,
    MEDIUM_RISK,
    HIGH_RISK,
    FORBIDDEN
}
```

#### Recovery Action Model

```java
public class RecoveryAction {
    private String actionId;
    private String sessionId;
    private String name;
    private String description;
    private RiskLevel riskLevel;
    private boolean requireManualApproval;
    private RecoveryActionStatus status;
    private String commandPreview;
    private String executionResult;
}
```

#### Recommended MVP Actions

| Action | Risk Level | MVP Behavior |
|---|---|---|
| Analyze jstack | READ_ONLY | Implement |
| Query metrics | READ_ONLY | Implement or mock |
| Generate diagnostic command | READ_ONLY | Implement |
| Clear local cache | LOW_RISK | Simulate |
| Pause scheduled task | MEDIUM_RISK | Simulate |
| Restart instance | HIGH_RISK | Recommendation only |
| Data repair | FORBIDDEN | Reject |

---

### 6.10 `incident-memory`

Responsible for generating and storing incident cards.

#### Incident Card Model

```java
public class IncidentCard {
    private String cardId;
    private String sessionId;
    private String title;
    private String symptom;
    private String rootCause;
    private List<String> keyEvidences;
    private List<String> recoveryActions;
    private List<String> preventionSuggestions;
    private List<String> relatedPatterns;
    private LocalDateTime createdAt;
}
```

#### Incident Card Template

```markdown
# Incident Card

## Fault Name

## Symptoms

## Impact

## Key Evidence

## Root Cause

## Recovery Actions

## Verification Result

## Prevention Suggestions

## Related Fault Patterns
```

---

## 7. REST API Design

### 7.1 Create Incident Session

```http
POST /api/incidents
```

Request:

```json
{
  "title": "接口超时，缓存刷新任务卡死",
  "description": "生产环境接口大量超时，怀疑缓存刷新异常",
  "severity": "HIGH"
}
```

Response:

```json
{
  "sessionId": "INC-20260430-0001",
  "status": "CREATED"
}
```

---

### 7.2 Upload Evidence

```http
POST /api/incidents/{sessionId}/evidences
```

Request:

```json
{
  "type": "JSTACK",
  "source": "manual-upload",
  "content": "Full thread dump Java HotSpot..."
}
```

---

### 7.3 Diagnose Incident

```http
POST /api/incidents/{sessionId}/diagnose
```

Response:

```json
{
  "reportId": "RPT-001",
  "summary": "当前故障高度疑似由 Redis 连接池耗尽引发...",
  "confidence": "HIGH",
  "candidateRootCauses": [],
  "recommendedActions": []
}
```

---

### 7.4 Get Diagnosis Report

```http
GET /api/incidents/{sessionId}/report
```

---

### 7.5 Generate Recovery Actions

```http
POST /api/incidents/{sessionId}/recovery-actions
```

---

### 7.6 Execute Recovery Action

```http
POST /api/incidents/{sessionId}/recovery-actions/{actionId}/execute
```

MVP behavior: simulated execution only.

---

### 7.7 Generate Incident Card

```http
POST /api/incidents/{sessionId}/incident-card
```

---

## 8. Database Design

### 8.1 `incident_session`

| Column | Type | Description |
|---|---|---|
| id | varchar | Session ID |
| title | varchar | Incident title |
| description | text | Incident description |
| severity | varchar | Severity level |
| status | varchar | Session status |
| created_at | datetime | Creation time |
| updated_at | datetime | Update time |

### 8.2 `evidence`

| Column | Type | Description |
|---|---|---|
| id | varchar | Evidence ID |
| session_id | varchar | Session ID |
| type | varchar | Evidence type |
| source | varchar | Evidence source |
| content | longtext | Raw evidence content |
| parsed_summary | longtext | Parsed summary |
| metadata_json | text | Metadata JSON |
| created_at | datetime | Creation time |

### 8.3 `diagnosis_report`

| Column | Type | Description |
|---|---|---|
| id | varchar | Report ID |
| session_id | varchar | Session ID |
| summary | text | Summary |
| confidence | varchar | Confidence level |
| report_json | longtext | Full structured report |
| created_at | datetime | Creation time |

### 8.4 `recovery_action`

| Column | Type | Description |
|---|---|---|
| id | varchar | Action ID |
| session_id | varchar | Session ID |
| name | varchar | Action name |
| risk_level | varchar | Risk level |
| require_approval | boolean | Whether manual approval is required |
| status | varchar | Action status |
| command_preview | text | Command preview |
| execution_result | text | Execution result |

### 8.5 `incident_card`

| Column | Type | Description |
|---|---|---|
| id | varchar | Card ID |
| session_id | varchar | Session ID |
| title | varchar | Card title |
| content_markdown | longtext | Markdown content |
| tags | varchar | Tags |
| created_at | datetime | Creation time |

### 8.6 `fault_pattern`

| Column | Type | Description |
|---|---|---|
| id | varchar | Pattern ID |
| name | varchar | Pattern name |
| description | text | Pattern description |
| pattern_json | longtext | Rule configuration |
| enabled | boolean | Whether enabled |

---

## 9. Fault Detection Rules

### 9.1 Redis Pool Exhausted

Pattern ID: `REDIS_POOL_EXHAUSTED`

Hit conditions:

```text
1. redis.active / redis.maxActive >= 0.9
2. redis.waiters > 0
3. Logs contain borrow timeout or command timeout
4. jstack contains RedisInputStream / Protocol.read / socketRead
```

Output:

```text
Fault pattern: REDIS_POOL_EXHAUSTED
Confidence: HIGH
```

---

### 9.2 Lock Contention

Pattern ID: `LOCK_CONTENTION`

Hit conditions:

```text
1. Multiple threads are WAITING or PARKING.
2. Multiple threads wait for the same lock object.
3. Waiting thread count exceeds threshold, for example 10.
4. Stack contains ReentrantReadWriteLock or AbstractQueuedSynchronizer.
```

Output:

```text
Fault pattern: LOCK_CONTENTION
Confidence: MEDIUM_HIGH
```

---

### 9.3 Full GC Pressure

Pattern ID: `FULL_GC_PRESSURE`

Hit conditions:

```text
1. Old generation usage > 85%.
2. Full GC count increases within a short time window.
3. GC time increases significantly.
4. Logs contain Allocation Failure or overhead limit.
```

Output:

```text
Fault pattern: FULL_GC_PRESSURE
Confidence: HIGH
```

---

### 9.4 Kafka Lag Increased

Pattern ID: `KAFKA_LAG_INCREASED`

Hit conditions:

```text
1. totalLag keeps increasing.
2. consumeRate is lower than produceRate if produceRate exists.
3. Consumer thread shows blocking or slow processing in jstack.
4. Logs contain rebalance or poll timeout.
```

Output:

```text
Fault pattern: KAFKA_LAG_INCREASED
Confidence: MEDIUM_HIGH
```

---

## 10. LLM Design

### 10.1 LlmClient Abstraction

```java
public interface LlmClient {
    LlmResponse complete(LlmRequest request);
}
```

```java
public class LlmRequest {
    private String prompt;
    private Map<String, Object> variables;
    private double temperature;
    private int maxTokens;
}
```

```java
public class LlmResponse {
    private String content;
    private String model;
    private int promptTokens;
    private int completionTokens;
}
```

### 10.2 Diagnosis Prompt

```text
你是一个资深 Java 生产故障诊断专家，擅长分析 jstack、JVM 指标、Redis、Kafka、DB 连接池、线程池和定时任务问题。

你的任务是基于输入的结构化证据，输出可验证的故障诊断报告。

要求：
1. 不允许编造输入中不存在的证据。
2. 每个根因判断必须关联至少一条证据。
3. 如果证据不足，必须明确说明还需要补充哪些信息。
4. 输出必须为合法 JSON。
5. 处置建议必须标注风险等级。
6. 需要区分“确定结论”和“候选推断”。

输入：
{{diagnosis_context}}

输出 JSON Schema：
{{schema}}
```

### 10.3 Expected Diagnosis JSON Output

```json
{
  "summary": "当前故障高度疑似由 Redis 连接池耗尽引发...",
  "confidence": "HIGH",
  "candidateRootCauses": [
    {
      "title": "Redis 连接池耗尽导致线程阻塞",
      "score": 0.91,
      "description": "Redis active 达到 maxActive...",
      "supportingEvidenceIds": ["EVD-001", "EVD-002"]
    }
  ],
  "evidenceChains": [
    {
      "conclusion": "Redis IO 阻塞是主因",
      "evidences": [
        "jstack 中大量线程停留在 RedisInputStream.ensureFill",
        "Redis active=80/80",
        "commandTimeoutCount 持续增长"
      ]
    }
  ],
  "recommendedActions": [
    {
      "title": "临时降低缓存刷新并发",
      "riskLevel": "LOW_RISK",
      "needApproval": true,
      "verification": "观察 Redis waiters 是否归零，接口超时是否下降"
    }
  ],
  "preventionSuggestions": [
    "禁止在读写锁内访问 Redis",
    "为 Redis 连接池增加 active/waiters 告警",
    "缓存刷新任务增加超时隔离"
  ]
}
```

---

## 11. OpenSpec Change Plan

Do not implement everything in one change. Use small incremental OpenSpec changes.

### 11.1 Change 1: `add-incident-session-core`

Goal:

```text
Establish the basic incident session and evidence management capability.
```

Scope:

```text
1. IncidentSession domain model
2. Evidence domain model
3. Create incident session API
4. Upload evidence API
5. Query incident session API
6. Basic persistence
7. Unit tests
```

Suggested prompt:

```text
Use the openspec-propose skill to create a change for:
新增 Thread Doctor 的故障会话核心能力，包括 IncidentSession、Evidence、创建会话接口、上传证据接口、查询会话接口和对应单元测试。
```

---

### 11.2 Change 2: `add-jstack-analyzer`

Goal:

```text
Support jstack upload and structured analysis.
```

Scope:

```text
1. JstackParser
2. ThreadDumpBlock parsing
3. Thread state statistics
4. Hot stack grouping
5. Lock contention detection
6. Redis/DB/Kafka IO blocking keyword recognition
7. Unit tests with sample jstack text
```

Suggested prompt:

```text
Use the openspec-propose skill to create a change for:
新增 Thread Doctor 的 jstack 结构化解析能力，包括线程块解析、线程状态统计、热点堆栈聚类、锁竞争识别、常见 IO 阻塞关键词识别和对应单元测试。
```

---

### 11.3 Change 3: `add-fault-pattern-detection`

Goal:

```text
Detect candidate fault patterns based on evidence and metrics.
```

Scope:

```text
1. FaultPattern model
2. FaultPatternDetector interface
3. RedisPoolExhaustedDetector
4. LockContentionDetector
5. FullGcPressureDetector
6. KafkaLagDetector
7. Candidate pattern scoring
8. Unit tests
```

Suggested prompt:

```text
Use the openspec-propose skill to create a change for:
新增 Thread Doctor 的故障模式识别能力，基于结构化证据和指标识别 Redis 连接池耗尽、锁竞争、Full GC 压力、Kafka 消费积压等候选故障模式，并输出评分和证据。
```

---

### 11.4 Change 4: `add-ai-diagnosis-report`

Goal:

```text
Generate AI-assisted diagnosis report from structured context.
```

Scope:

```text
1. LlmClient abstraction
2. PromptTemplate management
3. DiagnosisContext builder
4. DiagnosisReport generation
5. JSON output parsing and validation
6. Mock LLM unit tests
```

Suggested prompt:

```text
Use the openspec-propose skill to create a change for:
新增 Thread Doctor 的 AI 诊断报告生成能力，通过 LlmClient 抽象接口调用大模型，基于结构化证据、候选故障模式和历史案例生成 JSON 格式诊断报告，并提供 Mock LLM 单元测试。
```

---

### 11.5 Change 5: `add-recovery-recommendation`

Goal:

```text
Generate risk-classified recovery suggestions and simulated execution.
```

Scope:

```text
1. RecoveryAction model
2. RecoveryActionGenerator
3. Risk level rules
4. Simulated executor
5. Recovery verification model
6. Unit tests
```

Suggested prompt:

```text
Use the openspec-propose skill to create a change for:
新增 Thread Doctor 的处置建议与恢复验证能力，根据诊断报告生成风险分级处置动作，MVP 阶段仅支持模拟执行，并输出恢复验证方式。
```

---

### 11.6 Change 6: `add-incident-card-memory`

Goal:

```text
Generate reusable incident card after diagnosis and recovery.
```

Scope:

```text
1. IncidentCard model
2. IncidentCardGenerator
3. Markdown template
4. Historical incident card query
5. Related fault pattern tags
6. Unit tests
```

Suggested prompt:

```text
Use the openspec-propose skill to create a change for:
新增 Thread Doctor 的故障卡片沉淀能力，基于诊断报告、处置动作和恢复验证结果生成 Markdown 格式的故障复盘卡片，并支持历史卡片查询。
```

---

## 12. Recommended Project Structure

```text
thread-doctor/
├── pom.xml
├── openspec/
│   ├── project.md
│   ├── specs/
│   │   ├── incident-session/
│   │   ├── jstack-analyzer/
│   │   ├── fault-pattern/
│   │   ├── diagnosis-report/
│   │   ├── recovery-action/
│   │   └── incident-card/
│   └── changes/
│       ├── add-incident-session-core/
│       ├── add-jstack-analyzer/
│       ├── add-fault-pattern-detection/
│       ├── add-ai-diagnosis-report/
│       ├── add-recovery-recommendation/
│       └── add-incident-card-memory/
├── src/main/java/com/example/threaddoctor/
│   ├── ThreadDoctorApplication.java
│   ├── incident/
│   ├── evidence/
│   ├── jstack/
│   ├── metrics/
│   ├── pattern/
│   ├── diagnosis/
│   ├── agent/
│   ├── recovery/
│   ├── memory/
│   ├── llm/
│   └── common/
└── src/test/java/com/example/threaddoctor/
```

---

## 13. MVP Iteration Plan

### V0.1 Basic Incident Framework

Deliverables:

```text
1. Create incident session.
2. Upload evidence.
3. Query session detail.
4. Basic persistence.
```

Acceptance:

```text
1. REST APIs work.
2. Unit tests pass.
3. Evidence can be associated with incident session.
```

---

### V0.2 Jstack Analysis

Deliverables:

```text
1. Parse jstack text.
2. Count thread states.
3. Detect lock contention.
4. Detect hot stacks.
5. Detect common IO blocking keywords.
```

Acceptance:

```text
1. Given sample jstack, parser outputs correct total thread count.
2. Given lock contention sample, analyzer identifies same lock waiting group.
3. Given Redis blocking sample, analyzer marks Redis IO blocking.
```

---

### V0.3 Fault Pattern Detection

Deliverables:

```text
1. Implement detector interface.
2. Implement Redis pool exhausted detector.
3. Implement lock contention detector.
4. Implement Full GC pressure detector.
5. Implement Kafka lag detector.
```

Acceptance:

```text
1. Positive cases are detected.
2. Negative cases are not falsely detected.
3. Detection result includes confidence and evidence IDs.
```

---

### V0.4 AI Diagnosis Report

Deliverables:

```text
1. Build diagnosis context.
2. Call LlmClient.
3. Parse JSON diagnosis output.
4. Persist diagnosis report.
```

Acceptance:

```text
1. Mock LLM returns valid report.
2. Invalid JSON is rejected or handled gracefully.
3. Root cause must contain supporting evidence IDs.
```

---

### V0.5 Recovery Recommendation

Deliverables:

```text
1. Generate recovery actions.
2. Classify risk level.
3. Simulate execution.
4. Generate verification method.
```

Acceptance:

```text
1. High-risk actions require manual approval.
2. Forbidden actions cannot be executed.
3. Simulated action returns execution result.
```

---

### V1.0 Complete Demo Flow

Deliverables:

```text
1. Create incident.
2. Upload fault description, jstack, and metrics.
3. Run diagnosis.
4. Show root cause candidates and evidence chains.
5. Generate recovery suggestions.
6. Simulate recovery action.
7. Generate incident card.
```

Acceptance:

```text
The system can complete the full flow for the Redis pool exhausted + lock contention demo case.
```

---

## 14. Demo Case

### 14.1 Fault Scenario

```text
生产环境接口大量超时，缓存刷新任务长时间不结束，部分业务线程阻塞。
```

### 14.2 Log Evidence

```text
redis.clients.jedis.exceptions.JedisConnectionException: Could not get a resource from the pool
java.net.SocketTimeoutException: Read timed out
```

### 14.3 Jstack Evidence

```text
"cache-refresh-worker-1" RUNNABLE
  at redis.clients.jedis.util.RedisInputStream.ensureFill
  at redis.clients.jedis.Protocol.process
  at redis.clients.jedis.Jedis.get

"biz-worker-23" WAITING
  at jdk.internal.misc.Unsafe.park
  at java.util.concurrent.locks.LockSupport.park
  at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire
  at java.util.concurrent.locks.ReentrantReadWriteLock$ReadLock.lock
```

### 14.4 Redis Metrics Evidence

```json
{
  "maxActive": 80,
  "active": 80,
  "idle": 0,
  "waiters": 16,
  "borrowTimeoutCount": 42,
  "commandTimeoutCount": 203
}
```

### 14.5 Expected Diagnosis

```text
故障高度疑似由 Redis 连接池耗尽引发。
缓存刷新线程在锁内访问 Redis 后阻塞，导致其他业务线程等待读写锁，最终表现为接口超时。
```

### 14.6 Expected Recovery Suggestions

```text
1. 临时降低缓存刷新并发。
2. 暂停非核心缓存刷新任务。
3. 检查 Redis 慢查询和网络延迟。
4. 必要时重启异常实例释放阻塞线程。
5. 后续整改：禁止锁内远程调用，拆分锁粒度。
```

---

## 15. Codex Usage Guidance

### 15.1 Recommended Codex Workflow

Use the following workflow for every change:

```text
1. Use openspec-propose to create the change.
2. Review the generated spec and tasks.
3. Use openspec-implement to implement the change.
4. Run mvn test.
5. Fix all test failures.
6. Use openspec-archive only after the change is complete and verified.
```

### 15.2 Do Not Ask Codex to Implement Everything at Once

Avoid prompts like:

```text
Implement the whole Thread Doctor system.
```

Prefer prompts like:

```text
Implement only the add-jstack-analyzer change according to OpenSpec.
```

### 15.3 Required Validation Command

After each implementation step, Codex must run:

```bash
mvn test
```

If tests fail, Codex must fix the implementation or test cases until all tests pass.

---

## 16. Competition Presentation Keywords

Use these terms in presentation:

- Java production incident diagnosis
- AI-assisted root cause analysis
- Evidence-chain reasoning
- Rule + LLM hybrid diagnosis
- System immune mechanism
- Risk-classified self-healing
- Incident knowledge retention
- MTTR reduction
- Expert experience reuse
- Production reliability improvement

---

## 17. Final Recommended Scope

For the first competition version, implement these capabilities first:

```text
1. Incident session management
2. Evidence upload and management
3. Jstack structured analysis
4. Redis/JVM/Kafka metric snapshot input
5. Rule-based fault pattern detection
6. AI diagnosis report generation through LlmClient
7. Recovery recommendation generation
8. Incident card generation
```

Do not implement these in the first version:

```text
1. Real production restart
2. Real traffic switching
3. Real data repair
4. Full alert platform integration
5. Complex multi-tenant permission system
6. Complex knowledge graph construction
```

---

## 18. Final Project Slogan

```text
让系统会诊断、会处置、会复盘。
```

