/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.llm;

import jakarta.validation.constraints.Size;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record LlmConfigurationUpdateRequest(
        @Size(max = 512) String baseUrl,
        @Size(max = 512) String apiKey,
        @Size(max = 160) String model) {
}
