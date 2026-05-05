package com.geek.threaddoctor.prompt;

public class PromptTemplateLoadException extends PromptTemplateException {
    public PromptTemplateLoadException(PromptTemplateType templateType, String templatePath, Throwable cause) {
        super(templateType, templatePath, "Prompt template load failed: " + templateType + " at " + templatePath
                + ": " + cause.getMessage(), cause);
    }
}
