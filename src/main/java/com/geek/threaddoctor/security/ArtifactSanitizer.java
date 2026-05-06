package com.geek.threaddoctor.security;

import com.geek.threaddoctor.loganalysis.LogAnalysisProperties;
import com.geek.threaddoctor.loganalysis.SensitiveDataMasker;
import org.springframework.stereotype.Component;

@Component
public class ArtifactSanitizer {
    private final SensitiveDataMasker masker;
    private final SecurityLimitsProperties limits;

    public ArtifactSanitizer(SensitiveDataMasker masker, SecurityLimitsProperties limits) {
        this.masker = masker;
        this.limits = limits;
    }

    public String sanitize(String text, LogAnalysisProperties properties) {
        return limit(masker.mask(text, properties), limits.artifactOutputMaxLength());
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 14)) + "...[truncated]";
    }
}
