## ADDED Requirements

### Requirement: Template-backed diagnosis prompts
The system SHALL build diagnosis LLM prompts through the prompt template management layer.

#### Scenario: Build diagnosis prompt from templates
- **WHEN** a diagnosis report is requested for an incident session
- **THEN** the system loads the diagnosis system prompt, diagnosis user prompt template, and diagnosis JSON schema through typed prompt templates

#### Scenario: Render diagnosis prompt variables
- **WHEN** diagnosis prompt assembly runs
- **THEN** the user prompt template is rendered with `userGoal`, `incidentContext`, and `evidencePackJson` variables derived from the current diagnosis context

#### Scenario: Pass JSON schema to LLM client flow
- **WHEN** the diagnosis JSON schema template is loaded
- **THEN** the diagnosis flow uses that schema content as the expected structured response schema for the OpenAI-compatible client interaction

#### Scenario: Fail fast on invalid diagnosis prompt
- **WHEN** strict rendering is enabled and a diagnosis prompt template references a missing required variable
- **THEN** diagnosis generation fails before invoking the LLM client and returns a prompt-specific error
