## ADDED Requirements

### Requirement: Cached diagnosis progress
The system SHALL maintain session-scoped diagnosis progress in application cache while a diagnosis report is being generated. The system MUST NOT persist diagnosis progress to a database or external storage.

#### Scenario: Diagnosis progress starts
- **WHEN** diagnosis starts for an incident session
- **THEN** the system MUST store progress for that session with a running status, a non-zero percent, a current step, and an updated timestamp

#### Scenario: Diagnosis progress advances through backend phases
- **WHEN** the backend completes diagnosis phases such as context building, rule detection, LLM generation, report validation, and report persistence
- **THEN** the system MUST update cached progress with the corresponding step and a monotonically non-decreasing percent

#### Scenario: Diagnosis progress completes
- **WHEN** the diagnosis report is generated and cached successfully
- **THEN** the system MUST update cached progress to completed status with percent equal to 100

#### Scenario: Diagnosis progress fails
- **WHEN** diagnosis fails before returning a report
- **THEN** the system MUST update cached progress to failed status with the last known step, percent, and an error message before propagating the error

### Requirement: Diagnosis progress query
The system SHALL expose a read API that returns the current diagnosis progress for an incident session.

#### Scenario: Query running progress
- **WHEN** a client queries progress for a session with an in-flight diagnosis
- **THEN** the system MUST return the cached status, percent, step, message, and timestamps for that session

#### Scenario: Query completed progress
- **WHEN** a client queries progress after diagnosis has completed and the progress cache entry has not expired
- **THEN** the system MUST return completed status with percent equal to 100

#### Scenario: Query missing progress
- **WHEN** a client queries progress for a session that has no diagnosis progress entry
- **THEN** the system MUST return a deterministic not-started response or a not-found error that the frontend can handle without failing the whole conversation
