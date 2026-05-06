package com.geek.threaddoctor.loganalysis;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SensitiveDataMasker {
    private static final Pattern EMAIL = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern IPV4 = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    private static final Pattern SECRET_FIELD = Pattern.compile("(?i)\\b(password|passwd|pwd|token|secret|api[-_]?key|access[-_]?key|authorization|cookie|set-cookie)\\s*[:=]\\s*([^\\s,;]+)");
    private static final Pattern BEARER_TOKEN = Pattern.compile("(?i)bearer\\s+[A-Za-z0-9._~+/=-]+");

    public String mask(String text, LogAnalysisProperties properties) {
        if (text == null || text.isBlank() || !properties.maskingEnabled()) {
            return text;
        }
        String masked = EMAIL.matcher(text).replaceAll("[EMAIL]");
        masked = IPV4.matcher(masked).replaceAll("[IP]");
        masked = PHONE.matcher(masked).replaceAll("[PHONE]");
        masked = SECRET_FIELD.matcher(masked).replaceAll("$1=[SECRET]");
        masked = BEARER_TOKEN.matcher(masked).replaceAll("Bearer [SECRET]");
        return masked;
    }
}
