/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.llm;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record LlmConfigurationStatus(
        String activeSource,
        FieldStatus baseUrl,
        FieldStatus apiKey,
        FieldStatus model) {
    /**
     * 承载不可变业务数据。
     *
     * @author zhiwuli
     * @since 2026-05-07
     */
    public record FieldStatus(String value, boolean configuredByFrontend, String source) {
    }
}
