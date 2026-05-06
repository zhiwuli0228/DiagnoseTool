package com.geek.threaddoctor.llm;

public record LlmRuntimeConfiguration(String baseUrl, String apiKey, String model) {
    public boolean hasOverrides() {
        return hasText(baseUrl) || hasText(apiKey) || hasText(model);
    }

    static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
