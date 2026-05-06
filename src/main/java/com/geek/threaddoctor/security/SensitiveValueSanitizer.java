package com.geek.threaddoctor.security;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SensitiveValueSanitizer {
    private static final Pattern SECRET_ASSIGNMENT = Pattern.compile(
            "(?i)(api[-_]?key|access[-_]?key|authorization|token|secret|password|passwd|pwd)\\s*[:=]\\s*[^\\s,;\\]}]+");
    private static final Pattern BEARER_TOKEN = Pattern.compile("(?i)bearer\\s+[A-Za-z0-9._~+/=-]+");
    private static final Pattern SK_KEY = Pattern.compile("\\bsk-[A-Za-z0-9._-]{8,}\\b");

    public String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String sanitized = SECRET_ASSIGNMENT.matcher(value).replaceAll("$1=[SECRET]");
        sanitized = BEARER_TOKEN.matcher(sanitized).replaceAll("Bearer [SECRET]");
        sanitized = SK_KEY.matcher(sanitized).replaceAll("sk-[SECRET]");
        return sanitized;
    }
}
