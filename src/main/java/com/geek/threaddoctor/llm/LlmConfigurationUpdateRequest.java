package com.geek.threaddoctor.llm;

import jakarta.validation.constraints.Size;

public record LlmConfigurationUpdateRequest(
        @Size(max = 512) String baseUrl,
        @Size(max = 512) String apiKey,
        @Size(max = 160) String model) {
}
