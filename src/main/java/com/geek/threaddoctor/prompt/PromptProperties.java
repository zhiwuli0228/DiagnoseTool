package com.geek.threaddoctor.prompt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "thread-doctor.prompt")
public record PromptProperties(
        String templateDir,
        Boolean cacheEnabled,
        Boolean strictRendering,
        String defaultOutputLanguage) {
    public PromptProperties {
        cacheEnabled = cacheEnabled == null ? Boolean.TRUE : cacheEnabled;
        strictRendering = strictRendering == null ? Boolean.TRUE : strictRendering;
        defaultOutputLanguage = defaultOutputLanguage == null || defaultOutputLanguage.isBlank()
                ? "zh-CN" : defaultOutputLanguage;
    }
}
