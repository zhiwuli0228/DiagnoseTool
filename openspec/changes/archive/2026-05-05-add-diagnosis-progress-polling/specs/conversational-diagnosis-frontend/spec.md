## ADDED Requirements

### Requirement: Diagnosis progress polling
The frontend SHALL poll backend diagnosis progress while a diagnosis request is running for the active incident session.

#### Scenario: Polling starts with diagnosis
- **WHEN** the user starts diagnosis for an active incident session
- **THEN** the frontend MUST start polling the backend progress API while the diagnosis task remains running

#### Scenario: Polling updates workflow state
- **WHEN** the progress API returns a newer status, percent, step, or message
- **THEN** the frontend MUST update the conversation workflow state with the latest progress without losing submitted evidence or draft state

#### Scenario: Polling stops after diagnosis ends
- **WHEN** the diagnosis request succeeds, fails, or the component unmounts
- **THEN** the frontend MUST stop progress polling for that diagnosis task

#### Scenario: Progress polling failure is recoverable
- **WHEN** a progress polling request fails while the diagnosis request is still running
- **THEN** the frontend MUST keep the diagnosis request active and show a recoverable progress warning or retain the last known progress instead of failing the diagnosis workflow

### Requirement: Diagnosis progress display
The frontend SHALL show an accessible i18n-backed progress bar during diagnosis.

#### Scenario: Running diagnosis progress is visible
- **WHEN** diagnosis progress is available and the workflow stage is diagnosing
- **THEN** the frontend MUST display a progress bar with percent and a localized current step label

#### Scenario: Completed diagnosis progress is visible before report render
- **WHEN** backend progress reaches completed before the report response has rendered
- **THEN** the frontend MUST display completed progress until the report is available

#### Scenario: Failed diagnosis progress is visible
- **WHEN** backend progress reports failed status
- **THEN** the frontend MUST display a localized failed progress state with the backend error message when available

#### Scenario: Progress display is tested
- **WHEN** frontend tests run
- **THEN** they MUST verify progress polling, progress bar rendering, polling cleanup, and recoverable polling failures
