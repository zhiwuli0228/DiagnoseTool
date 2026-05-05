请基于诊断结果生成 codeagent investigation task。

要求：
1. 任务必须具体；
2. 只引用日志中出现过的类名、方法名、异常类型、traceId；
3. 明确要求 codeagent 在代码仓中验证这些类和方法是否存在；
4. 明确要求补充单元测试；
5. 明确禁止 codeagent 基于日志直接假设代码实现。