package com.geek.threaddoctor.llm;

public record LlmConfigurationStatus(
        String activeSource,
        FieldStatus baseUrl,
        FieldStatus apiKey,
        FieldStatus model) {
    public record FieldStatus(String value, boolean configuredByFrontend, String source) {
    }
}
