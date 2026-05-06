/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.prompt;

import java.time.Instant;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record PromptTemplate(
        PromptTemplateType templateType,
        String path,
        String content,
        PromptContentType contentType,
        PromptTemplateSource loadedFrom,
        Instant loadedAt) {
}
