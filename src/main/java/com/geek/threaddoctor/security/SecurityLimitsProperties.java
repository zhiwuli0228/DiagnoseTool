package com.geek.threaddoctor.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "thread-doctor.security")
public record SecurityLimitsProperties(
        int idMaxLength,
        int titleMaxLength,
        int descriptionMaxLength,
        int sourceMaxLength,
        int evidenceContentMaxLength,
        int metadataMaxLength,
        int metricsJsonMaxLength,
        int llmBaseUrlMaxLength,
        int llmApiKeyMaxLength,
        int llmModelMaxLength,
        int directoryPathMaxLength,
        int artifactOutputMaxLength) {
    public SecurityLimitsProperties {
        idMaxLength = idMaxLength <= 0 ? 96 : idMaxLength;
        titleMaxLength = titleMaxLength <= 0 ? 120 : titleMaxLength;
        descriptionMaxLength = descriptionMaxLength <= 0 ? 2000 : descriptionMaxLength;
        sourceMaxLength = sourceMaxLength <= 0 ? 200 : sourceMaxLength;
        evidenceContentMaxLength = evidenceContentMaxLength <= 0 ? 200_000 : evidenceContentMaxLength;
        metadataMaxLength = metadataMaxLength <= 0 ? 20_000 : metadataMaxLength;
        metricsJsonMaxLength = metricsJsonMaxLength <= 0 ? 100_000 : metricsJsonMaxLength;
        llmBaseUrlMaxLength = llmBaseUrlMaxLength <= 0 ? 512 : llmBaseUrlMaxLength;
        llmApiKeyMaxLength = llmApiKeyMaxLength <= 0 ? 512 : llmApiKeyMaxLength;
        llmModelMaxLength = llmModelMaxLength <= 0 ? 160 : llmModelMaxLength;
        directoryPathMaxLength = directoryPathMaxLength <= 0 ? 1024 : directoryPathMaxLength;
        artifactOutputMaxLength = artifactOutputMaxLength <= 0 ? 500_000 : artifactOutputMaxLength;
    }
}
