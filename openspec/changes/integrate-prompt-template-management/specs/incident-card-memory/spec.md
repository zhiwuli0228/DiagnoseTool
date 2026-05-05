## ADDED Requirements

### Requirement: Template-backed incident review generation
The system SHALL generate incident review style Markdown documents by rendering the configured incident review prompt template.

#### Scenario: Render incident review from diagnosis context
- **WHEN** incident review document generation is requested after diagnosis
- **THEN** the system renders `INCIDENT_REVIEW` using Evidence Pack and Diagnosis Report variables

#### Scenario: Include review sections
- **WHEN** the incident review template is rendered
- **THEN** the output includes incident summary, impact, diagnosis conclusion, key evidence, timeline, recovery actions, follow-up items, and lessons learned sections

#### Scenario: Keep review document-only
- **WHEN** incident review Markdown is generated
- **THEN** the system returns document content only and does not execute commands, mutate files, or submit follow-up changes automatically
