package com.geek.threaddoctor.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

class OpenAiCompatibleRestClientTest {
    @Test
    void createsRequestFactoryWithoutProxyByDefault() {
        SimpleClientHttpRequestFactory factory =
                OpenAiCompatibleRestClient.createRequestFactory(1000, 2000, false, "", 0);

        assertThat(factory).isNotNull();
    }

    @Test
    void createsRequestFactoryWithProxyWhenEnabled() {
        SimpleClientHttpRequestFactory factory =
                OpenAiCompatibleRestClient.createRequestFactory(1000, 2000, true, "127.0.0.1", 7890);

        assertThat(factory).isNotNull();
    }

    @Test
    void rejectsEnabledProxyWithoutHostAndPort() {
        assertThatThrownBy(() -> OpenAiCompatibleRestClient.createRequestFactory(1000, 2000, true, "", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LLM proxy host and port are required");
    }

    @Test
    void includesRequestVariablesInUserContent() {
        LlmRuntimeConfigurationService configurationService =
                new LlmRuntimeConfigurationService("https://example.test", "test-key", "test-model");
        OpenAiCompatibleRestClient client = new OpenAiCompatibleRestClient(
                "https://example.test", "test-key", "test-model", 1000, 2000, false, "", 0,
                configurationService, new ObjectMapper());

        String content = client.buildUserContent(new LlmRequest("diagnose", Map.of("detections", "redis"), 0.2, 100));

        assertThat(content).contains("diagnose");
        assertThat(content).contains("\"detections\":\"redis\"");
    }

    @Test
    void buildsRequestBodyWithEffectiveModel() {
        LlmRuntimeConfigurationService configurationService =
                new LlmRuntimeConfigurationService("https://example.test", "test-key", "backend-model");
        configurationService.save(new LlmConfigurationUpdateRequest(null, null, "frontend-model"));
        OpenAiCompatibleRestClient client = new OpenAiCompatibleRestClient(
                "https://example.test", "test-key", "backend-model", 1000, 2000, false, "", 0,
                configurationService, new ObjectMapper());

        Map<String, Object> body = client.buildRequestBody(
                new LlmRequest("diagnose", Map.of(), 0.2, 100),
                configurationService.effectiveConfiguration());

        assertThat(body).containsEntry("model", "frontend-model");
    }
}
