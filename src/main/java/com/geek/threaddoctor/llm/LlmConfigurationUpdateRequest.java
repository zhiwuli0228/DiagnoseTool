package com.geek.threaddoctor.llm;

public record LlmConfigurationUpdateRequest(String baseUrl, String apiKey, String model) {
}
