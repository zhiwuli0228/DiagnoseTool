## ADDED Requirements

### Requirement: Frontend LLM Settings UI
The system SHALL provide a frontend settings UI where an operator can view and edit runtime LLM configuration fields including `baseUrl`, `api-key`, and `model`.

#### Scenario: Save LLM settings from frontend
- **WHEN** an operator enters valid `baseUrl`, `api-key`, and `model` values and saves them
- **THEN** the system MUST store those values as runtime LLM configuration for subsequent backend LLM requests

#### Scenario: View masked API key state
- **WHEN** an operator opens the LLM settings UI after an API key has been configured
- **THEN** the system MUST show that an API key is configured without displaying the full API key value

### Requirement: Hot-Effective Runtime LLM Overrides
The system SHALL apply frontend-provided LLM configuration to subsequent LLM requests without requiring backend service restart.

#### Scenario: Model change takes effect
- **WHEN** an operator saves a different `model` value from the frontend
- **THEN** the next backend LLM request MUST use the new model value

#### Scenario: Provider endpoint change takes effect
- **WHEN** an operator saves a different `baseUrl` value from the frontend
- **THEN** the next backend LLM request MUST use the new provider endpoint

### Requirement: Backend Configuration Fallback
The system SHALL use backend service configuration for any LLM field that is not configured by the frontend.

#### Scenario: No frontend configuration exists
- **WHEN** no frontend LLM configuration has been saved
- **THEN** backend LLM requests MUST use the backend service configured `baseUrl`, `api-key`, and `model`

#### Scenario: Partial frontend configuration exists
- **WHEN** the frontend configuration only provides a non-blank `model`
- **THEN** backend LLM requests MUST use the frontend `model` and backend-configured `baseUrl` and `api-key`

### Requirement: Clear Frontend LLM Configuration
The system SHALL allow an operator to clear frontend-provided LLM configuration and return to backend service configuration.

#### Scenario: Clear runtime configuration
- **WHEN** an operator clears the frontend LLM configuration
- **THEN** subsequent backend LLM requests MUST use backend service configuration for all LLM fields

### Requirement: LLM Configuration API Safety
The system SHALL provide backend APIs for reading, saving, and clearing runtime LLM configuration without exposing full API key values in read responses.

#### Scenario: Read configuration status
- **WHEN** the frontend reads the current LLM configuration status
- **THEN** the backend MUST return active source information and masked API key status without returning the full API key

#### Scenario: Reject invalid configuration
- **WHEN** an operator saves an invalid `baseUrl` or blank override value
- **THEN** the backend MUST reject the configuration with a clear validation error
