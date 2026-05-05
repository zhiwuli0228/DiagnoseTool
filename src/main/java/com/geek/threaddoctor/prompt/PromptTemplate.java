package com.geek.threaddoctor.prompt;

import java.time.Instant;

public record PromptTemplate(
        PromptTemplateType templateType,
        String path,
        String content,
        PromptContentType contentType,
        PromptTemplateSource loadedFrom,
        Instant loadedAt) {
}
