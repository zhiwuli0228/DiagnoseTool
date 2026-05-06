package com.geek.threaddoctor.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LlmRuntimeConfigurationServiceTest {
    @Test
    void usesBackendDefaultsWhenNoFrontendOverridesExist() {
        LlmRuntimeConfigurationService service =
                new LlmRuntimeConfigurationService("https://backend.test/v1", "backend-key", "backend-model");

        EffectiveLlmConfiguration effective = service.effectiveConfiguration();

        assertThat(effective.baseUrl()).isEqualTo("https://backend.test/v1");
        assertThat(effective.apiKey()).isEqualTo("backend-key");
        assertThat(effective.model()).isEqualTo("backend-model");
        assertThat(service.status().activeSource()).isEqualTo("backend");
    }

    @Test
    void supportsPartialFrontendOverride() {
        LlmRuntimeConfigurationService service =
                new LlmRuntimeConfigurationService("https://backend.test/v1", "backend-key", "backend-model");

        service.save(new LlmConfigurationUpdateRequest(null, null, "frontend-model"));

        EffectiveLlmConfiguration effective = service.effectiveConfiguration();
        assertThat(effective.baseUrl()).isEqualTo("https://backend.test/v1");
        assertThat(effective.apiKey()).isEqualTo("backend-key");
        assertThat(effective.model()).isEqualTo("frontend-model");
        assertThat(service.status().model().source()).isEqualTo("frontend");
    }

    @Test
    void supportsFrontendBaseUrlAndModelOverrideButKeepsApiKeyFromEnvironment() {
        LlmRuntimeConfigurationService service =
                new LlmRuntimeConfigurationService("https://backend.test/v1", "backend-key", "backend-model");

        LlmConfigurationStatus status = service.save(new LlmConfigurationUpdateRequest(
                "https://frontend.test/v1", null, "frontend-model"));

        EffectiveLlmConfiguration effective = service.effectiveConfiguration();
        assertThat(effective.baseUrl()).isEqualTo("https://frontend.test/v1");
        assertThat(effective.apiKey()).isEqualTo("backend-key");
        assertThat(effective.model()).isEqualTo("frontend-model");
        assertThat(status.apiKey().value()).isEqualTo("back****-key");
        assertThat(status.apiKey().configuredByFrontend()).isFalse();
        assertThat(service.storedFrontendOverrides().hasOverrides()).isTrue();
    }

    @Test
    void clearRestoresBackendDefaults() {
        LlmRuntimeConfigurationService service =
                new LlmRuntimeConfigurationService("https://backend.test/v1", "backend-key", "backend-model");
        service.save(new LlmConfigurationUpdateRequest("https://frontend.test/v1", null, "frontend-model"));

        service.clear();

        assertThat(service.effectiveConfiguration().model()).isEqualTo("backend-model");
        assertThat(service.status().activeSource()).isEqualTo("backend");
    }

    @Test
    void rejectsInvalidBaseUrlAndBlankOverride() {
        LlmRuntimeConfigurationService service =
                new LlmRuntimeConfigurationService("https://backend.test/v1", "backend-key", "backend-model");

        assertThatThrownBy(() -> service.save(new LlmConfigurationUpdateRequest("ftp://bad.test", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
        assertThatThrownBy(() -> service.save(new LlmConfigurationUpdateRequest(null, "frontend-key", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LLM_API_KEY");
    }
}
