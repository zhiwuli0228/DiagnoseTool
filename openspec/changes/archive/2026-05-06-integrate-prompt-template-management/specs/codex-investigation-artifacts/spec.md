## ADDED Requirements

### Requirement: Template-backed Codex task generation
The system SHALL generate Codex investigation task Markdown by rendering the configured Codex task prompt template.

#### Scenario: Render Codex task from Evidence Pack
- **WHEN** a client requests Codex task generation for a parsed log session
- **THEN** the system renders `CODEX_INVESTIGATION_TASK` using Evidence Pack variables instead of assembling fixed Markdown inline

#### Scenario: Preserve engineering constraints
- **WHEN** the Codex task template is rendered
- **THEN** the output includes engineering constraints for JDK 21, Maven, JUnit 5 with Mockito, no PowerMock, evidence-based investigation, and running `mvn test`

### Requirement: Template-backed OpenSpec draft generation
The system SHALL generate OpenSpec change draft Markdown by rendering the configured OpenSpec draft prompt template.

#### Scenario: Render OpenSpec draft from Evidence Pack
- **WHEN** a client requests OpenSpec change draft generation for a parsed log session
- **THEN** the system renders `OPENSPEC_CHANGE_DRAFT` using Evidence Pack variables instead of assembling fixed Markdown inline

#### Scenario: Keep draft document-only
- **WHEN** the OpenSpec draft template is rendered
- **THEN** the system returns generated document content only and does not create, modify, apply, or archive repository OpenSpec files

### Requirement: Generated artifact prompt safety
The system SHALL preserve existing generated artifact safety guarantees when templates are used.

#### Scenario: Mask and bound rendered artifact prompts
- **WHEN** generated artifact templates receive Evidence Pack content containing long excerpts or sensitive values
- **THEN** rendered output remains bounded and masked according to existing Evidence Pack output limits
