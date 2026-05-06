# codex-investigation-artifacts Specification

## Purpose
TBD - created by archiving change add-log-intelligence-evidence-pack. Update Purpose after archive.
## Requirements
### Requirement: Codex investigation task generation
The system SHALL generate a bounded Markdown investigation task for Codex from an Evidence Pack.

#### Scenario: Generate Codex task Markdown
- **WHEN** a client requests a Codex task for a session with an Evidence Pack
- **THEN** the system returns Markdown containing Title, Incident Summary, Key Evidence, Timeline, Suspected Code Areas, Questions to Answer, Required Codebase Investigation, Required Changes if Root Cause Confirmed, Tests to Add, Engineering Constraints, and Do Not sections

#### Scenario: Include engineering constraints
- **WHEN** Codex task Markdown is generated
- **THEN** the system includes constraints for JDK 21, Maven, JUnit 5 with Mockito, no PowerMock, no guessing without evidence, no unrelated changes, and running `mvn test`

#### Scenario: Keep generated task bounded and masked
- **WHEN** Evidence Pack content includes long excerpts or sensitive values
- **THEN** the generated task applies output caps and sensitive data masking before returning Markdown

### Requirement: OpenSpec change draft generation
The system SHALL generate OpenSpec change draft content from an Evidence Pack without writing or submitting repository changes automatically.

#### Scenario: Generate OpenSpec draft content
- **WHEN** a client requests an OpenSpec change draft for a session with an Evidence Pack
- **THEN** the system returns draft proposal, tasks, and spec delta content containing Why, What Changes, Impact, Risk, Rollback, and testable requirements derived from the evidence

#### Scenario: Do not auto-submit OpenSpec changes
- **WHEN** OpenSpec draft generation completes
- **THEN** the system returns generated content only and does not create, modify, archive, or apply OpenSpec change files in the repository

### Requirement: Generated artifact safety
The system SHALL treat generated Codex tasks and OpenSpec drafts as documents only.

#### Scenario: No generated artifact execution
- **WHEN** a generated task or draft contains commands, paths, or suggested code changes
- **THEN** the system does not execute commands, read source code, mutate files, or invoke external tools as part of generation

#### Scenario: Surface generation limitations
- **WHEN** generated artifacts are returned
- **THEN** the system includes limitations explaining that conclusions are based only on supplied log evidence and require codebase verification

### Requirement: Generated artifact API access
The system SHALL allow clients to request and review generated investigation artifacts as document API responses without auto-executing them.

#### Scenario: Generate artifacts as documents
- **WHEN** a client requests Codex task or OpenSpec draft generation for a parsed log session
- **THEN** the system returns generated document content as Markdown response data

#### Scenario: Keep artifact actions document-only
- **WHEN** a generated artifact is returned
- **THEN** the system does not execute commands, mutate repository files, or submit generated OpenSpec changes automatically

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

