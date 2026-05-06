## ADDED Requirements

### Requirement: Prompt template catalog
The system SHALL define a typed catalog of supported prompt templates with stable template type, default path, content type, and description metadata.

#### Scenario: List supported template types
- **WHEN** backend prompt assembly needs a diagnosis, generated artifact, or review prompt
- **THEN** it selects one of the typed template entries instead of using an ad hoc file path or inline prompt identifier

#### Scenario: Identify content type
- **WHEN** a prompt template is loaded
- **THEN** the system exposes whether the template content is Markdown, JSON, or plain text according to the typed catalog

### Requirement: Prompt template resources
The system SHALL package default prompt templates as classpath resources under `src/main/resources/prompts`.

#### Scenario: Load packaged diagnosis templates
- **WHEN** no external template directory is configured
- **THEN** the system loads diagnosis system prompt, diagnosis user prompt, and diagnosis JSON schema from classpath resources

#### Scenario: Load packaged generated artifact templates
- **WHEN** generated artifact prompt assembly runs without external overrides
- **THEN** the system loads Codex task, OpenSpec change draft, and incident review templates from classpath resources

### Requirement: Prompt template loading
The system SHALL load prompt templates from an optional configured external directory before falling back to classpath defaults.

#### Scenario: External template overrides classpath default
- **WHEN** `thread-doctor.prompt.template-dir` points to a directory containing a template path for the requested type
- **THEN** the system loads that external file and records the source as `EXTERNAL_FILE`

#### Scenario: Classpath fallback
- **WHEN** no external directory is configured or the external file does not exist
- **THEN** the system loads the catalog default classpath resource and records the source as `CLASSPATH`

#### Scenario: Template load failure
- **WHEN** a requested prompt template cannot be loaded from either external file or classpath
- **THEN** the system fails with a prompt-specific load or not-found exception containing template type and path information

### Requirement: Prompt rendering
The system SHALL render prompt templates containing `{{variableName}}` placeholders from supplied variables.

#### Scenario: Render simple variables
- **WHEN** a template contains `{{userGoal}}` and the render request provides `userGoal`
- **THEN** the rendered content includes the provided value in place of the placeholder

#### Scenario: Render nested variables
- **WHEN** a template contains a nested placeholder such as `{{incident.sessionId}}`
- **THEN** the renderer resolves the value from nested map or object variables

#### Scenario: Render JSON object variables
- **WHEN** a Markdown prompt contains a variable whose value is a structured object or map
- **THEN** the renderer inserts a deterministic JSON representation of that variable

#### Scenario: Strict unresolved variables
- **WHEN** strict rendering is enabled and a template contains an unresolved placeholder
- **THEN** the renderer fails with a missing variable exception listing unresolved variables

#### Scenario: Relaxed unresolved variables
- **WHEN** strict rendering is disabled and a template contains an unresolved placeholder
- **THEN** the renderer returns rendered content and records unresolved variables in the render result

### Requirement: Prompt assembly
The system SHALL assemble typed prompt outputs for diagnosis, Codex task generation, OpenSpec draft generation, and incident review generation.

#### Scenario: Assemble diagnosis prompt
- **WHEN** diagnosis prompt assembly receives an Evidence Pack and diagnosis request context
- **THEN** it returns a diagnosis prompt containing system prompt, rendered user prompt, and JSON schema content

#### Scenario: Assemble Codex task prompt
- **WHEN** Codex task prompt assembly receives an Evidence Pack
- **THEN** it returns rendered Markdown using incident summary, key evidence, timeline, suspected code areas, and recommended checks

#### Scenario: Assemble OpenSpec draft prompt
- **WHEN** OpenSpec draft prompt assembly receives an Evidence Pack
- **THEN** it returns rendered Markdown using evidence-derived title, why, changes, impact, risk, rollback, and acceptance criteria variables

#### Scenario: Assemble incident review prompt
- **WHEN** incident review prompt assembly receives an Evidence Pack and diagnosis report
- **THEN** it returns rendered Markdown containing incident summary, diagnosis conclusion, key evidence, impact, timeline, recovery, and follow-up sections

### Requirement: Prompt configuration
The system SHALL provide configuration for prompt template directory, prompt cache behavior, strict rendering, and default output language.

#### Scenario: Defaults are applied
- **WHEN** no prompt configuration is provided
- **THEN** the system uses classpath templates, enables caching, enables strict rendering, and uses `zh-CN` as the default output language

#### Scenario: Cache disabled for local debugging
- **WHEN** prompt caching is disabled
- **THEN** the loader reloads template content for subsequent render requests instead of using cached content

### Requirement: Prompt error diagnostics
The system SHALL expose prompt-specific errors with enough context to diagnose template failures.

#### Scenario: Missing variable error
- **WHEN** rendering fails because required variables are missing
- **THEN** the error includes template type and missing variable names

#### Scenario: Load error
- **WHEN** loading fails because a template path is unreadable or invalid
- **THEN** the error includes template type, template path, and a root cause message
