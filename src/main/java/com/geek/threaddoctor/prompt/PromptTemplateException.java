package com.geek.threaddoctor.prompt;

public class PromptTemplateException extends RuntimeException {
    private final PromptTemplateType templateType;
    private final String templatePath;

    public PromptTemplateException(PromptTemplateType templateType, String templatePath, String message) {
        super(message);
        this.templateType = templateType;
        this.templatePath = templatePath;
    }

    public PromptTemplateException(PromptTemplateType templateType, String templatePath, String message, Throwable cause) {
        super(message, cause);
        this.templateType = templateType;
        this.templatePath = templatePath;
    }

    public PromptTemplateType templateType() {
        return templateType;
    }

    public String templatePath() {
        return templatePath;
    }
}
