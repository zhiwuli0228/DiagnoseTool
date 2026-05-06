/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.prompt;

/**
 * 定义固定的业务取值。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public enum PromptTemplateType {
    DIAGNOSIS_SYSTEM_PROMPT("diagnosis-system-prompt", "prompts/diagnosis/system-prompt.md", PromptContentType.MARKDOWN,
            "Diagnosis system prompt"),
    DIAGNOSIS_USER_PROMPT("diagnosis-user-prompt", "prompts/diagnosis/user-prompt-template.md", PromptContentType.MARKDOWN,
            "Diagnosis user prompt template"),
    DIAGNOSIS_JSON_SCHEMA("diagnosis-json-schema", "prompts/diagnosis/json-schema.json", PromptContentType.JSON,
            "Diagnosis JSON response schema"),
    CODEX_INVESTIGATION_TASK("codex-investigation-task", "prompts/codex-task/codex-investigation-task-template.md", PromptContentType.MARKDOWN,
            "Codex investigation task template"),
    DIAGNOSIS_CODEBASE_INVESTIGATION("diagnosis-codebase-investigation", "prompts/codex-task/diagnosis-codebase-investigation-template.md", PromptContentType.MARKDOWN,
            "Diagnosis handoff prompt for codebase investigation"),
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

    /**
     * 获取模板标识。
     *
     * @return 文本结果
     */
    public String templateId() {
        return templateId;
    }

    /**
     * 获取默认模板路径。
     *
     * @return 文本结果
     */
    public String defaultPath() {
        return defaultPath;
    }

    /**
     * 获取内容类型。
     *
     * @return 业务处理结果
     */
    public PromptContentType contentType() {
        return contentType;
    }

    /**
     * 获取描述信息。
     *
     * @return 文本结果
     */
    public String description() {
        return description;
    }
}
