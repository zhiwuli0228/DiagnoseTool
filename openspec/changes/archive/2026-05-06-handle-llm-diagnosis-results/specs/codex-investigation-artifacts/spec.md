## ADDED Requirements

### Requirement: Diagnosis report codebase prompt generation
The system SHALL generate codebase investigation prompts from unresolved diagnosis reports as document API response data.

#### Scenario: Generate prompt from diagnosis report
- **WHEN** a diagnosis report is unresolved and contains evidence, suspected components, or unresolved questions
- **THEN** the system MUST generate a bounded Markdown prompt suitable for Codex/OpenCode codebase investigation

#### Scenario: Include investigation boundaries
- **WHEN** the codebase prompt is generated from a diagnosis report
- **THEN** it MUST include instructions to verify evidence against the codebase, avoid guessing, preserve user evidence, and avoid unrelated changes

#### Scenario: Preserve generated artifact safety
- **WHEN** the codebase prompt is returned
- **THEN** the system MUST apply existing generated artifact masking, output limits, and document-only safety guarantees
