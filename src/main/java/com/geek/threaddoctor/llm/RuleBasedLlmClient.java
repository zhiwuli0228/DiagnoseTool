package com.geek.threaddoctor.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(LlmClient.class)
public class RuleBasedLlmClient implements LlmClient {
    @Override
    public LlmResponse complete(LlmRequest request) {
        // 未启用远程 LLM 时使用固定 JSON，保证本地调试闭环可运行。
        String json = """
                {
                  "summary": "Rule-based fallback diagnosis completed with available evidence.",
                  "confidence": "MEDIUM_HIGH",
                  "localizationStatus": "UNRESOLVED",
                  "unresolvedReasons": ["No external LLM is configured, so codebase-aware localization is not available."],
                  "candidateRootCauses": [],
                  "evidenceChains": [],
                  "recommendedActions": []
                }
                """;
        return new LlmResponse(json, "rule-based-fallback", 0, 0);
    }
}
