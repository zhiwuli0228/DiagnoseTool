## MODIFIED Requirements

### Requirement: LLM Configuration API Safety
The system SHALL provide backend APIs for reading, saving, and clearing runtime LLM configuration without accepting frontend-provided API keys or exposing full API key values in read responses.

#### Scenario: Read configuration status
- **WHEN** the frontend reads the current LLM configuration status
- **THEN** the backend MUST return active source information and masked API key status without returning the full API key

#### Scenario: Reject invalid configuration
- **WHEN** an operator saves an invalid `baseUrl` or blank override value
- **THEN** the backend MUST reject the configuration with a clear validation error

#### Scenario: Reject frontend API key override
- **WHEN** an operator saves a frontend-provided `api-key`
- **THEN** the backend MUST reject the value and require API keys to come from the `LLM_API_KEY` environment variable

#### Scenario: Avoid plaintext secret disclosure in validation errors
- **WHEN** saving LLM configuration fails validation or processing
- **THEN** the backend MUST NOT include the submitted API key or secret-like values in the error response
