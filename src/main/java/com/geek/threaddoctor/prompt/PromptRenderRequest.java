package com.geek.threaddoctor.prompt;

import java.util.Map;

public record PromptRenderRequest(
        PromptTemplateType templateType,
        String templateContent,
        Map<String, Object> variables,
        boolean strict) {
}
