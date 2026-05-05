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
        OpenAiCompatibleRestClient client = new OpenAiCompatibleRestClient(
                "https://example.test", "test-key", "test-model", 1000, 2000, false, "", 0, new ObjectMapper());

        String content = client.buildUserContent(new LlmRequest("diagnose", Map.of("detections", "redis"), 0.2, 100));

        assertThat(content).contains("diagnose");
        assertThat(content).contains("\"detections\":\"redis\"");
    }
}
