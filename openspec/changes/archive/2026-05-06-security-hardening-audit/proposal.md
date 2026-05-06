## Why

Security is a prerequisite for broader adoption of Thread Doctor. The current application accepts multiple frontend-driven inputs and runtime secrets, so the system needs a clear security hardening contract before implementation continues.

## What Changes

- Add strict backend validation for all frontend-originated inputs, including JSON payloads, query/path parameters, uploaded files, search filters, LLM settings, evidence text, metrics snapshots, and generated-artifact requests.
- Add request size, collection size, string length, numeric range, path, URL, archive, and enum constraints to reduce DoS and malformed-input risk.
- Ensure API keys are read from the `LLM_API_KEY` environment variable instead of frontend requests or YAML files.
- Ensure API keys and sensitive plaintext values are never returned to the frontend, logs, reports, or generated documents.
- Add boundary handling for invalid IDs, oversized payloads, unsafe paths, archive bombs, repeated expensive operations, and unsupported file or content types.
- Add focused automated tests for validation rejection, masking/redaction, secret non-disclosure, and configured boundary limits.

## Capabilities

### New Capabilities
- `security-hardening-audit`: Cross-cutting security requirements for strict frontend input validation, local secret protection, sensitive-output redaction, and boundary limits.

### Modified Capabilities
- `frontend-llm-configuration`: Runtime LLM API key handling must be hardened so keys come from `LLM_API_KEY`, frontend API key overrides are rejected, and keys are never returned as plaintext.
- `log-intelligence-evidence-pack`: Log upload, decompression, parsing, search, and generated outputs must enforce stronger DoS boundaries and sensitive-data controls.

## Impact

- Backend controllers, DTOs, validation annotations, exception responses, and service-level guards.
- Frontend forms and API client behavior where validation errors need to be surfaced without leaking sensitive values.
- Runtime LLM configuration storage and status responses.
- Log analysis upload, ZIP parsing, directory upload, search, evidence pack, Codex task, and OpenSpec draft generation paths.
- Tests covering API contracts, security boundaries, and sensitive data handling.
