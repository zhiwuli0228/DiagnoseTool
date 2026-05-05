package com.geek.threaddoctor.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class PromptTestFactory {
    private PromptTestFactory() {
    }

    public static PromptAssemblyService assemblyService() {
        ObjectMapper objectMapper = new ObjectMapper();
        PromptProperties properties = new PromptProperties(null, true, true, "zh-CN");
        PromptTemplateLoader loader = new PromptTemplateLoader(properties);
        PromptRenderer renderer = new PromptRenderer(objectMapper);
        return new PromptAssemblyService(loader, renderer, properties, objectMapper);
    }
}
