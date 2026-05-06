package com.geek.threaddoctor.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "thread-doctor.llm", name = "enabled", havingValue = "true")
public class OpenAiCompatibleRestClient implements LlmClient {
    private static final String JSON_ONLY_SYSTEM_PROMPT = """
            You are a Java production incident diagnosis assistant.
            Return exactly one JSON object and no explanatory text.
            The JSON object must contain string fields: summary, confidence.
            confidence must be one of: LOW, MEDIUM, MEDIUM_HIGH, HIGH.
            """;

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final LlmRuntimeConfigurationService configurationService;

    public OpenAiCompatibleRestClient(
            @Value("${thread-doctor.llm.base-url}") String baseUrl,
            @Value("${thread-doctor.llm.model}") String model,
            @Value("${thread-doctor.llm.connect-timeout-ms:10000}") int connectTimeoutMs,
            @Value("${thread-doctor.llm.read-timeout-ms:60000}") int readTimeoutMs,
            @Value("${thread-doctor.llm.proxy.enabled:false}") boolean proxyEnabled,
            @Value("${thread-doctor.llm.proxy.host:}") String proxyHost,
            @Value("${thread-doctor.llm.proxy.port:0}") int proxyPort,
            LlmRuntimeConfigurationService configurationService,
            ObjectMapper objectMapper) {
        this.restClientBuilder = RestClient.builder()
                .requestFactory(createRequestFactory(connectTimeoutMs, readTimeoutMs, proxyEnabled, proxyHost, proxyPort));
        this.objectMapper = objectMapper;
        this.configurationService = configurationService;
    }

    static SimpleClientHttpRequestFactory createRequestFactory(int connectTimeoutMs, int readTimeoutMs,
                                                               boolean proxyEnabled, String proxyHost, int proxyPort) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        if (proxyEnabled) {
            if (proxyHost == null || proxyHost.isBlank() || proxyPort <= 0) {
                throw new IllegalArgumentException("LLM proxy host and port are required when proxy is enabled");
            }
            // 代理只在显式开启时生效，避免生产环境误走本地调试代理。
            requestFactory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }
        return requestFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LlmResponse complete(LlmRequest request) {
        EffectiveLlmConfiguration configuration = configurationService.effectiveConfiguration();
        Map<String, Object> body = buildRequestBody(request, configuration);
        Map<String, Object> response = clientFor(configuration).post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + configuration.apiKey())
                .body(body)
                .retrieve()
                .body(Map.class);
        if (response == null) {
            throw new IllegalStateException("Empty LLM response");
        }
        return new LlmResponse(extractContent(response), configuration.model(), 0, 0);
    }

    Map<String, Object> buildRequestBody(LlmRequest request, EffectiveLlmConfiguration configuration) {
        return Map.of(
                "model", configuration.model(),
                "temperature", request.temperature(),
                "max_tokens", request.maxTokens(),
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", JSON_ONLY_SYSTEM_PROMPT),
                        Map.of("role", "user", "content", buildUserContent(request))));
    }

    RestClient clientFor(EffectiveLlmConfiguration configuration) {
        return restClientBuilder.clone()
                .baseUrl(configuration.baseUrl())
                .build();
    }

    String buildUserContent(LlmRequest request) {
        return request.prompt()
                + "\n\nUse the following diagnosis context. Preserve referenced evidence ids.\n"
                + toJson(request.variables());
    }

    private String toJson(Map<String, Object> variables) {
        try {
            return objectMapper.writeValueAsString(variables == null ? Map.of() : variables);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize LLM request variables", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getOrDefault("choices", List.of());
        if (choices.isEmpty()) {
            return "";
        }
        Map<String, Object> choice = choices.get(0);
        Object message = choice.get("message");
        if (message instanceof Map<?, ?> messageMap) {
            Object content = messageMap.get("content");
            return content == null ? "" : String.valueOf(content);
        }
        Object text = choice.get("text");
        return text == null ? "" : String.valueOf(text);
    }
}
