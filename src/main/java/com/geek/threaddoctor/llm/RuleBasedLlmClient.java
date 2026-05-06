/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Component
@ConditionalOnMissingBean(LlmClient.class)
public class RuleBasedLlmClient implements LlmClient {
    /**
     * 完成当前操作。
     *
     * @param request 请求数据
     * @return 操作结果
     */
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
