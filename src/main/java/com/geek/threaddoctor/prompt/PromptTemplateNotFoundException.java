package com.geek.threaddoctor.prompt;

public class PromptTemplateNotFoundException extends PromptTemplateException {
    public PromptTemplateNotFoundException(PromptTemplateType templateType, String templatePath) {
        super(templateType, templatePath, "Prompt template not found: " + templateType + " at " + templatePath);
    }
}
