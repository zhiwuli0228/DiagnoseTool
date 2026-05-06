# frontend-llm-configuration Specification

## Purpose
TBD - created by archiving change add-frontend-llm-configuration. Update Purpose after archive.
## Requirements
### Requirement: Frontend LLM Settings UI
The system SHALL provide a frontend settings UI where an operator can view and edit runtime LLM configuration fields including `baseUrl` and `model`.

#### Scenario: Save LLM settings from frontend
- **WHEN** an operator enters valid `baseUrl` and `model` values and saves them
- **THEN** the system MUST store those values as runtime LLM configuration for subsequent backend LLM requests

#### Scenario: View masked API key state
- **WHEN** an operator opens the LLM settings UI after an API key has been configured
- **THEN** the system MUST show that `LLM_API_KEY` is configured without displaying the full API key value

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
- **THEN** backend LLM requests MUST use backend service configured `baseUrl` and `model` plus API key from `LLM_API_KEY`

#### Scenario: Partial frontend configuration exists
- **WHEN** the frontend configuration only provides a non-blank `model`
- **THEN** backend LLM requests MUST use the frontend `model`, backend-configured `baseUrl`, and API key from `LLM_API_KEY`

### Requirement: Clear Frontend LLM Configuration
The system SHALL allow an operator to clear frontend-provided LLM configuration and return to backend service configuration.

#### Scenario: Clear runtime configuration
- **WHEN** an operator clears the frontend LLM configuration
- **THEN** subsequent backend LLM requests MUST use backend service configuration for all LLM fields

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

