package com.geek.threaddoctor.prompt;

import java.util.List;

public class MissingPromptVariableException extends PromptRenderException {
    private final List<String> missingVariables;

    public MissingPromptVariableException(PromptTemplateType templateType, List<String> missingVariables) {
        super(templateType, "Missing prompt variables for " + templateType + ": " + missingVariables, null);
        this.missingVariables = List.copyOf(missingVariables);
    }

    public List<String> missingVariables() {
        return missingVariables;
    }
}
