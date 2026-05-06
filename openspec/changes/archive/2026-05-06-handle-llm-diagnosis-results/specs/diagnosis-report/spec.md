## ADDED Requirements

### Requirement: Diagnosis localization status
The system SHALL include explicit localization status and next-action metadata in diagnosis report responses.

#### Scenario: Return localized report metadata
- **WHEN** a diagnosis report has enough evidence to locate the problem
- **THEN** the report response MUST include `localizationStatus=LOCALIZED` and supporting evidence references

#### Scenario: Return unresolved report metadata
- **WHEN** a diagnosis report cannot locate the problem from supplied evidence
- **THEN** the report response MUST include `localizationStatus=UNRESOLVED`, unresolved reasons, and next-step options

#### Scenario: Return follow-up evidence metadata
- **WHEN** additional information is required before continuing diagnosis
- **THEN** the report response MUST include structured follow-up evidence requests that the frontend can render

#### Scenario: Return codebase prompt metadata
- **WHEN** a codebase investigation prompt is available
- **THEN** the report response MUST include the generated prompt and a document-only warning for Codex/OpenCode handoff

### Requirement: LLM diagnosis response validation
The system SHALL validate LLM diagnosis output before presenting it as a final diagnosis.

#### Scenario: Validate structured localization result
- **WHEN** the LLM client returns structured diagnosis content
- **THEN** the backend MUST validate localization status, confidence, evidence references, and next-step fields before caching the report

#### Scenario: Fallback for incomplete LLM output
- **WHEN** the LLM output omits required localization fields or cannot be parsed
- **THEN** the backend MUST create an unresolved diagnosis result with clear limitations instead of presenting an unsupported final conclusion
