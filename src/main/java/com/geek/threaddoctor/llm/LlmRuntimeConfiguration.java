package com.geek.threaddoctor.llm;

public record LlmRuntimeConfiguration(String baseUrl, String model) {
    public boolean hasOverrides() {
        return hasText(baseUrl) || hasText(model);
    }

    static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
