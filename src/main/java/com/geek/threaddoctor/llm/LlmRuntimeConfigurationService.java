package com.geek.threaddoctor.llm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LlmRuntimeConfigurationService {
    private final EffectiveLlmConfiguration backendDefaults;
    private final AtomicReference<LlmRuntimeConfiguration> frontendOverrides =
            new AtomicReference<>(new LlmRuntimeConfiguration(null, null, null));

    public LlmRuntimeConfigurationService(
            @Value("${thread-doctor.llm.base-url:}") String backendBaseUrl,
            @Value("${thread-doctor.llm.api-key:}") String backendApiKey,
            @Value("${thread-doctor.llm.model:}") String backendModel) {
        this.backendDefaults = new EffectiveLlmConfiguration(
                normalizeBackendValue(backendBaseUrl),
                normalizeBackendValue(backendApiKey),
                normalizeBackendValue(backendModel));
        if (LlmRuntimeConfiguration.hasText(this.backendDefaults.baseUrl())) {
            validateBaseUrl(this.backendDefaults.baseUrl());
        }
    }

    public LlmConfigurationStatus status() {
        LlmRuntimeConfiguration overrides = frontendOverrides.get();
        EffectiveLlmConfiguration effective = effectiveConfiguration();
        return new LlmConfigurationStatus(
                overrides.hasOverrides() ? "frontend" : "backend",
                fieldStatus(effective.baseUrl(), overrides.baseUrl(), false),
                fieldStatus(effective.apiKey(), overrides.apiKey(), true),
                fieldStatus(effective.model(), overrides.model(), false));
    }

    public LlmConfigurationStatus save(LlmConfigurationUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("LLM configuration request is required");
        }
        String baseUrl = normalizeOverride("baseUrl", request.baseUrl());
        String apiKey = normalizeOverride("api-key", request.apiKey());
        String model = normalizeOverride("model", request.model());
        if (baseUrl != null) {
            validateBaseUrl(baseUrl);
        }
        frontendOverrides.set(new LlmRuntimeConfiguration(baseUrl, apiKey, model));
        validateEffectiveConfiguration(effectiveConfiguration());
        return status();
    }

    public LlmConfigurationStatus clear() {
        frontendOverrides.set(new LlmRuntimeConfiguration(null, null, null));
        return status();
    }

    public EffectiveLlmConfiguration effectiveConfiguration() {
        LlmRuntimeConfiguration overrides = frontendOverrides.get();
        EffectiveLlmConfiguration effective = new EffectiveLlmConfiguration(
                choose(overrides.baseUrl(), backendDefaults.baseUrl()),
                choose(overrides.apiKey(), backendDefaults.apiKey()),
                choose(overrides.model(), backendDefaults.model()));
        validateEffectiveConfiguration(effective);
        return effective;
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

    private String requireBackendValue(String propertyName, String value) {
        if (!LlmRuntimeConfiguration.hasText(value)) {
            throw new IllegalArgumentException("Backend LLM configuration is missing: " + propertyName);
        }
        return value.trim();
    }

    private void validateEffectiveConfiguration(EffectiveLlmConfiguration configuration) {
        requireBackendValue("effective baseUrl", configuration.baseUrl());
        validateBaseUrl(configuration.baseUrl());
        requireBackendValue("effective api-key", configuration.apiKey());
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
