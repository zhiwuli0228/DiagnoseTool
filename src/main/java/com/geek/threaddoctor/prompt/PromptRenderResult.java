/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.prompt;

import java.time.Instant;
import java.util.List;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record PromptRenderResult(
        PromptTemplateType templateType,
        String renderedContent,
        List<String> unresolvedVariables,
        Instant renderedAt) {
}
