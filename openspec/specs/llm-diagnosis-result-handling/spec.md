# llm-diagnosis-result-handling Specification

## Purpose
TBD - created by archiving change handle-llm-diagnosis-results. Update Purpose after archive.
## Requirements
### Requirement: Diagnosis localization assessment
The system SHALL assess whether the current evidence and LLM diagnosis output are sufficient to locate the problem.

#### Scenario: Diagnosis is clearly localized
- **WHEN** LLM processing returns a diagnosis with a concrete root cause, supporting evidence IDs, affected component, and actionable remediation
- **THEN** the system MUST mark the diagnosis localization status as `LOCALIZED`

#### Scenario: Diagnosis is not clearly localized
- **WHEN** LLM processing cannot identify a concrete root cause or cannot connect supplied evidence to an affected code path
- **THEN** the system MUST mark the diagnosis localization status as `UNRESOLVED`
- **AND** include one or more unresolved reasons

#### Scenario: Diagnosis needs more evidence
- **WHEN** the LLM output indicates missing logs, missing jstack, missing metrics, missing trace IDs, unclear time range, or insufficient context
- **THEN** the system MUST mark the result as requiring additional information and return specific follow-up evidence requests

### Requirement: Follow-up evidence requests
The system SHALL produce clear follow-up requirements when diagnosis cannot continue safely without more user-provided information.

#### Scenario: Request specific evidence
- **WHEN** diagnosis is unresolved due to missing or ambiguous evidence
- **THEN** the backend MUST return a bounded list of requested evidence items with title, reason, expected format, and example guidance

#### Scenario: Continue diagnosis after evidence submission
- **WHEN** the user submits requested evidence and re-runs diagnosis
- **THEN** the system MUST include both existing and newly submitted evidence in the next diagnosis attempt

### Requirement: Codebase investigation prompt handoff
The system SHALL generate a bounded Markdown prompt for Codex/OpenCode when diagnosis cannot be directly localized from supplied evidence.

#### Scenario: Generate codebase prompt for unresolved diagnosis
- **WHEN** diagnosis is unresolved and sufficient evidence exists to guide codebase investigation
- **THEN** the backend MUST return a Markdown prompt that summarizes the incident, key evidence, suspected areas, questions to answer, codebase search instructions, constraints, and limitations

#### Scenario: Keep codebase prompt document-only
- **WHEN** the backend generates a codebase investigation prompt
- **THEN** it MUST NOT execute commands, inspect source files, call Codex/OpenCode, or mutate repository files

#### Scenario: Bound and mask prompt content
- **WHEN** the generated codebase prompt includes evidence excerpts or user-provided content
- **THEN** the backend MUST apply configured output limits and sensitive-data masking before returning the prompt

#### Scenario: No prompt when more evidence is required first
- **WHEN** diagnosis is unresolved because essential evidence is missing
- **THEN** the backend MUST prioritize follow-up evidence requests and MAY omit the codebase prompt until minimum evidence requirements are met

