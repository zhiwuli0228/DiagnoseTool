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

