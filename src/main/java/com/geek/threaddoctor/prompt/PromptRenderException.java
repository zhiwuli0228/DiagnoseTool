package com.geek.threaddoctor.prompt;

public class PromptRenderException extends PromptTemplateException {
    public PromptRenderException(PromptTemplateType templateType, String message, Throwable cause) {
        super(templateType, templateType == null ? "" : templateType.defaultPath(), message, cause);
    }
}
