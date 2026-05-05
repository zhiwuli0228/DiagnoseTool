package com.geek.threaddoctor.prompt;

public enum PromptTemplateType {
    DIAGNOSIS_SYSTEM_PROMPT("diagnosis-system-prompt", "prompts/diagnosis/system-prompt.md", PromptContentType.MARKDOWN,
            "Diagnosis system prompt"),
    DIAGNOSIS_USER_PROMPT("diagnosis-user-prompt", "prompts/diagnosis/user-prompt-template.md", PromptContentType.MARKDOWN,
            "Diagnosis user prompt template"),
    DIAGNOSIS_JSON_SCHEMA("diagnosis-json-schema", "prompts/diagnosis/json-schema.json", PromptContentType.JSON,
            "Diagnosis JSON response schema"),
    CODEX_INVESTIGATION_TASK("codex-investigation-task", "prompts/codex-task/codex-investigation-task-template.md", PromptContentType.MARKDOWN,
            "Codex investigation task template"),
    OPENSPEC_CHANGE_DRAFT("openspec-change-draft", "prompts/openspec/openspec-change-draft-template.md", PromptContentType.MARKDOWN,
            "OpenSpec change draft template"),
    INCIDENT_REVIEW("incident-review", "prompts/review/incident-review-template.md", PromptContentType.MARKDOWN,
            "Incident review document template");

    private final String templateId;
    private final String defaultPath;
    private final PromptContentType contentType;
    private final String description;

    PromptTemplateType(String templateId, String defaultPath, PromptContentType contentType, String description) {
        this.templateId = templateId;
        this.defaultPath = defaultPath;
        this.contentType = contentType;
        this.description = description;
    }

    public String templateId() {
        return templateId;
    }

    public String defaultPath() {
        return defaultPath;
    }

    public PromptContentType contentType() {
        return contentType;
    }

    public String description() {
        return description;
    }
}
