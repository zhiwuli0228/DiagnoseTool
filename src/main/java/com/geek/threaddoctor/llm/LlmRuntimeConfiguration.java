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
public record LlmRuntimeConfiguration(String baseUrl, String model) {
    /**
     * 判断是否存在配置覆盖。
     *
     * @return 条件成立时返回真，否则返回假
     */
    public boolean hasOverrides() {
        return hasText(baseUrl) || hasText(model);
    }

    static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
