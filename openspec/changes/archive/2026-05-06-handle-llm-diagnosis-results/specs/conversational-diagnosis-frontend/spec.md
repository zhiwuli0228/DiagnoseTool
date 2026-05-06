## ADDED Requirements

### Requirement: Unresolved diagnosis choices
The frontend SHALL present explicit next-step choices when a diagnosis report cannot clearly locate the problem.

#### Scenario: Show unresolved diagnosis state
- **WHEN** the backend returns a diagnosis report with `localizationStatus=UNRESOLVED`
- **THEN** the frontend MUST show the unresolved reasons and available next-step choices

#### Scenario: Continue by providing key information
- **WHEN** unresolved diagnosis includes follow-up evidence requests
- **THEN** the frontend MUST render the requested information clearly and allow the user to submit additional evidence before running diagnosis again

#### Scenario: Copy codebase investigation prompt
- **WHEN** unresolved diagnosis includes a codebase investigation prompt
- **THEN** the frontend MUST render the prompt and provide a copy action for the user

#### Scenario: Avoid automatic codebase tool execution
- **WHEN** the user views or copies the codebase investigation prompt
- **THEN** the frontend MUST NOT invoke Codex, OpenCode, shell commands, or repository mutations automatically

### Requirement: Diagnosis result action tests
The frontend SHALL include tests for unresolved diagnosis handling.

#### Scenario: Test follow-up evidence choice
- **WHEN** frontend tests receive an unresolved diagnosis with follow-up evidence requests
- **THEN** they MUST verify the requests are visible and the user can continue evidence submission

#### Scenario: Test codebase prompt copy
- **WHEN** frontend tests receive an unresolved diagnosis with a codebase investigation prompt
- **THEN** they MUST verify the prompt is visible and copy behavior is available without executing tools
