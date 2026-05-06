/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.llm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@Service
public class LlmRuntimeConfigurationService {
    private final EffectiveLlmConfiguration backendDefaults;
    private final AtomicReference<LlmRuntimeConfiguration> frontendOverrides =
            new AtomicReference<>(new LlmRuntimeConfiguration(null, null));

    public LlmRuntimeConfigurationService(
            @Value("${thread-doctor.llm.base-url:}") String backendBaseUrl,
            @Value("${LLM_API_KEY:}") String backendApiKey,
            @Value("${thread-doctor.llm.model:}") String backendModel) {
        this.backendDefaults = new EffectiveLlmConfiguration(
                normalizeBackendValue(backendBaseUrl),
                normalizeBackendValue(backendApiKey),
                normalizeBackendValue(backendModel));
        if (LlmRuntimeConfiguration.hasText(this.backendDefaults.baseUrl())) {
            validateBaseUrl(this.backendDefaults.baseUrl());
        }
    }

    /**
     * 获取状态。
     *
     * @return 配置状态
     */
    public LlmConfigurationStatus status() {
        LlmRuntimeConfiguration overrides = frontendOverrides.get();
        EffectiveLlmConfiguration effective = effectiveConfiguration();
        return new LlmConfigurationStatus(
                overrides.hasOverrides() ? "frontend" : "backend",
                fieldStatus(effective.baseUrl(), overrides.baseUrl(), false),
                new LlmConfigurationStatus.FieldStatus(maskSecret(effective.apiKey()), false, "backend"),
                fieldStatus(effective.model(), overrides.model(), false));
    }

    /**
     * 保存业务记录。
     *
     * @param request 请求数据
     * @return 业务处理结果
     */
    public LlmConfigurationStatus save(LlmConfigurationUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("LLM configuration request is required");
        }
        String baseUrl = normalizeOverride("baseUrl", request.baseUrl());
        rejectApiKeyOverride(request.apiKey());
        String model = normalizeOverride("model", request.model());
        if (baseUrl != null) {
            validateBaseUrl(baseUrl);
        }
        frontendOverrides.set(new LlmRuntimeConfiguration(baseUrl, model));
        validateEffectiveConfiguration(effectiveConfiguration());
        return status();
    }

    /**
     * 清除运行时配置。
     *
     * @return 清除后的配置状态
     */
    public LlmConfigurationStatus clear() {
        frontendOverrides.set(new LlmRuntimeConfiguration(null, null));
        return status();
    }

    /**
     * 获取生效配置。
     *
     * @return 生效配置
     */
    public EffectiveLlmConfiguration effectiveConfiguration() {
        LlmRuntimeConfiguration overrides = frontendOverrides.get();
        EffectiveLlmConfiguration effective = new EffectiveLlmConfiguration(
                choose(overrides.baseUrl(), backendDefaults.baseUrl()),
                backendDefaults.apiKey(),
                choose(overrides.model(), backendDefaults.model()));
        validateEffectiveConfiguration(effective);
        return effective;
    }

    LlmRuntimeConfiguration storedFrontendOverrides() {
        return frontendOverrides.get();
    }

    private LlmConfigurationStatus.FieldStatus fieldStatus(String effectiveValue, String overrideValue, boolean secret) {
        boolean configuredByFrontend = LlmRuntimeConfiguration.hasText(overrideValue);
        return new LlmConfigurationStatus.FieldStatus(
                secret ? maskSecret(effectiveValue) : effectiveValue,
                configuredByFrontend,
                configuredByFrontend ? "frontend" : "backend");
    }

    private String choose(String overrideValue, String backendValue) {
        return LlmRuntimeConfiguration.hasText(overrideValue) ? overrideValue.trim() : backendValue;
    }

    private String normalizeBackendValue(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeOverride(String fieldName, String value) {
        if (value == null) {
            return null;
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("LLM " + fieldName + " override must not be blank");
        }
        return value.trim();
    }

    private void rejectApiKeyOverride(String value) {
        if (value != null && !value.isBlank()) {
            throw new IllegalArgumentException("LLM api-key must be provided by environment variable LLM_API_KEY");
        }
    }

    private String requireBackendValue(String propertyName, String value) {
        if (!LlmRuntimeConfiguration.hasText(value)) {
            throw new IllegalArgumentException("Backend LLM configuration is missing: " + propertyName);
        }
        return value.trim();
    }

    private void validateEffectiveConfiguration(EffectiveLlmConfiguration configuration) {
        requireBackendValue("effective baseUrl", configuration.baseUrl());
        validateBaseUrl(configuration.baseUrl());
        requireBackendValue("environment variable LLM_API_KEY", configuration.apiKey());
        requireBackendValue("effective model", configuration.model());
    }

    private void validateBaseUrl(String baseUrl) {
        try {
            URI uri = new URI(baseUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException("LLM baseUrl must be a valid http or https URL");
            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("LLM baseUrl must be a valid http or https URL", ex);
        }
    }

    private String maskSecret(String value) {
        if (!LlmRuntimeConfiguration.hasText(value)) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 8) {
            return "****";
        }
        return trimmed.substring(0, 4) + "****" + trimmed.substring(trimmed.length() - 4);
    }
}
