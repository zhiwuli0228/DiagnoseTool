package com.geek.threaddoctor.prompt;

import java.time.Instant;
import java.util.List;

public record PromptRenderResult(
        PromptTemplateType templateType,
        String renderedContent,
        List<String> unresolvedVariables,
        Instant renderedAt) {
}
