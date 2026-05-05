请基于以下 Incident Diagnosis Context 进行生产故障诊断。

输出要求：

1. 先给出一句话结论。
2. 输出故障严重程度判断。
3. 输出 Top 3 根因候选，按可能性排序。
4. 每个根因候选必须包含：
    - 结论
    - 置信度
    - 支持证据
    - 反证或不确定点
    - 建议验证动作
5. 输出故障时间线摘要。
6. 输出关键证据链。
7. 输出排除项。
8. 输出临时处置建议。
9. 输出长期整改建议。
10. 输出需要补充的信息。
11. 输出给 Codex 的代码走读任务。
12. 输出给 OpenSpec 的变更草案建议。
13. 不允许使用输入中不存在的信息。
14. 如果证据不足，必须明确写“证据不足”，不能强行下结论。

请使用以下 Markdown 结构输出：

# 诊断结论

## 一句话结论

{{one_sentence_conclusion}}

## 严重程度

- Level: {{P0/P1/P2/P3}}
- Reason: {{reason}}

# 根因候选 Top 3

## Root Cause 1: {{title}}

- Confidence: {{HIGH/MEDIUM/LOW}}
- Type: {{Redis/DB/Kafka/JVM/ThreadPool/Lock/Network/Application/Unknown}}
- Conclusion: {{conclusion}}

### Supporting Evidence

| Evidence ID | Evidence | Why It Matters |
|---|---|---|
| {{evidenceId}} | {{summary}} | {{reason}} |

### Uncertainty / Counter Evidence

{{uncertainty}}

### Validation Steps

| Step | How to Verify | Expected Result | If Not Matched |
|---|---|---|---|
| 1 | {{command_or_check}} | {{expected}} | {{fallback_analysis}} |

## Root Cause 2: {{title}}

同上。

## Root Cause 3: {{title}}

同上。

# 故障时间线摘要

| Time | Event | Evidence |
|---|---|---|
| {{time}} | {{event}} | {{evidenceId}} |

# 关键证据链

1. {{evidence_chain_step_1}}
2. {{evidence_chain_step_2}}
3. {{evidence_chain_step_3}}

# 排除项

| Candidate Cause | Current Judgment | Reason |
|---|---|---|
| Full GC | 暂不支持 / 证据不足 / 可能 | {{reason}} |
| DB Pool Exhaustion | 暂不支持 / 证据不足 / 可能 | {{reason}} |
| Kafka Lag | 暂不支持 / 证据不足 / 可能 | {{reason}} |
| Redis Pool Exhaustion | 暂不支持 / 证据不足 / 可能 | {{reason}} |
| CPU Saturation | 暂不支持 / 证据不足 / 可能 | {{reason}} |

# 临时处置建议

| Priority | Action | Risk Level | Expected Effect | Verification |
|---|---|---|---|---|
| P1 | {{action}} | {{LOW/MEDIUM/HIGH}} | {{effect}} | {{verify}} |

# 长期整改建议

| Item | Recommendation | Related Evidence | Suggested Owner |
|---|---|---|---|
| 1 | {{recommendation}} | {{evidenceId}} | {{owner_type}} |

# 需要补充的信息

1. {{missing_info_1}}
2. {{missing_info_2}}

# 给 Codex 的代码走读任务

## Problem

{{problem_summary}}

## Evidence

{{evidence_summary}}

## Suspected Code Areas

{{suspected_code_areas}}

## Questions for Codex

1. {{question_1}}
2. {{question_2}}
3. {{question_3}}

## Required Checks

1. 检查日志中出现的类和方法是否存在。
2. 检查异常链路是否和代码调用链一致。
3. 检查是否存在锁内远程调用、连接池耗尽、线程池队列堆积、超时未隔离等问题。
4. 检查是否已有单元测试覆盖该异常场景。
5. 如果确认根因，给出最小风险修复方案和测试用例。

# OpenSpec Change Draft 建议

## Change Title

{{change_title}}

## Why

{{why}}

## What Changes

{{what_changes}}

## Impact

{{impact}}

## Acceptance Criteria

1. {{acceptance_criteria_1}}
2. {{acceptance_criteria_2}}

## Tests

1. {{test_1}}
2. {{test_2}}