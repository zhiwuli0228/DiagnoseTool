# security-hardening-audit Specification

## Purpose
TBD - created by archiving change security-hardening-audit. Update Purpose after archive.
## Requirements
### Requirement: Frontend input validation
The system SHALL validate all frontend-originated API inputs before executing business logic.

#### Scenario: Reject invalid JSON body fields
- **WHEN** a frontend request submits missing required fields, blank identifiers, invalid enum values, invalid URLs, invalid timestamps, negative numeric values, oversized strings, or oversized collections
- **THEN** the backend MUST reject the request with a structured validation error before invoking the target service operation

#### Scenario: Reject invalid path and query parameters
- **WHEN** a frontend request submits a path parameter or query parameter with an invalid format, excessive length, or unsafe characters for that endpoint
- **THEN** the backend MUST reject the request with a structured validation error

#### Scenario: Bound generated artifact requests
- **WHEN** a frontend request asks the backend to generate a diagnosis report, incident card, Codex task, OpenSpec draft, or review artifact
- **THEN** the backend MUST enforce configured input and output bounds before returning generated content

### Requirement: Denial-of-service boundaries
The system SHALL enforce configured safety limits on expensive or untrusted operations.

#### Scenario: Reject oversized request content
- **WHEN** a frontend request includes uploaded files, text evidence, metrics payloads, log search filters, prompt variables, or generated-artifact inputs that exceed configured size limits
- **THEN** the backend MUST reject the request or truncate only explicitly allowed output fields with a visible truncation marker

#### Scenario: Reject repeated or broad expensive requests
- **WHEN** a frontend request would trigger broad log scanning, archive parsing, search, clustering, timeline generation, evidence pack generation, or LLM prompt construction beyond configured limits
- **THEN** the backend MUST fail fast with a structured safety error or apply a configured maximum limit

#### Scenario: Preserve normal bounded workflows
- **WHEN** frontend requests stay within configured bounds
- **THEN** the backend MUST continue to process the request without requiring new user-visible security steps

### Requirement: Local sensitive data protection
The system SHALL avoid locally retaining frontend-provided secrets and SHALL redact sensitive personal information from returned derived outputs.

#### Scenario: Reject frontend-provided API keys
- **WHEN** the backend receives an API key from the frontend
- **THEN** the backend MUST reject the value and require the `LLM_API_KEY` environment variable

#### Scenario: Do not return plaintext secrets
- **WHEN** the frontend reads configuration status, session state, generated reports, errors, or derived artifacts
- **THEN** the backend MUST NOT return plaintext API keys, tokens, passwords, or equivalent secrets

#### Scenario: Redact sensitive personal data in outputs
- **WHEN** backend-generated outputs include personal information or credential-like values detected in user-provided evidence or logs
- **THEN** the backend MUST return redacted or masked values instead of raw sensitive values

### Requirement: Security regression coverage
The system SHALL include automated tests for validation, sensitive data handling, and safety boundary behavior.

#### Scenario: Validate rejected frontend inputs
- **WHEN** automated tests submit malformed or oversized frontend API inputs
- **THEN** the tests MUST verify the backend rejects them with structured errors and does not execute the protected operation

#### Scenario: Verify secret non-disclosure
- **WHEN** automated tests save or process frontend-provided secrets
- **THEN** the tests MUST verify frontend-provided API keys are rejected and frontend-visible responses never contain plaintext secrets

#### Scenario: Verify boundary enforcement
- **WHEN** automated tests submit unsafe paths, archive traversal attempts, archive bombs, unsupported content types, excessive search limits, or excessive generated-output requests
- **THEN** the tests MUST verify configured boundaries are enforced

