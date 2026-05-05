package com.geek.threaddoctor.llm;

import java.util.Map;

public record LlmRequest(String prompt, Map<String, Object> variables, double temperature, int maxTokens) {
}
