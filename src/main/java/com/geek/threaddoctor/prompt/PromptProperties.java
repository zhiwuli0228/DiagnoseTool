/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.prompt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
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
