## 1. Validation Inventory

- [x] 1.1 Inventory all frontend-facing backend endpoints, request DTOs, multipart uploads, path variables, and query parameters.
- [x] 1.2 Define per-field limits for identifiers, titles, notes, evidence content, metrics payloads, search filters, LLM settings, generated artifact requests, file names, and uploaded content.
- [x] 1.3 Document which invalid inputs must be rejected and which bounded outputs may be truncated with a marker.

## 2. API Boundary Validation

- [x] 2.1 Add Bean Validation annotations and `@Valid` handling to frontend-facing request DTOs and controller entry points.
- [x] 2.2 Add validation for path variables and query parameters, including ID format, length, enum values, timestamps, and numeric ranges.
- [x] 2.3 Normalize API validation errors through the existing exception handling path without echoing submitted secret values.
- [x] 2.4 Update frontend API error display so validation failures are visible without exposing rejected sensitive input.

## 3. Secret Protection

- [x] 3.1 Require LLM API keys to come from the `LLM_API_KEY` environment variable instead of frontend or YAML configuration.
- [x] 3.2 Update runtime LLM configuration storage so frontend-provided API keys are rejected and only `baseUrl`/`model` can be overridden.
- [x] 3.3 Ensure LLM configuration status responses return only source metadata and masked key state, never plaintext API keys.
- [x] 3.4 Ensure validation errors, logs, generated reports, incident cards, Codex tasks, and OpenSpec drafts do not include plaintext secrets.

## 4. Log and Artifact Boundaries

- [x] 4.1 Harden ZIP and directory upload validation for content type, supported suffixes, entry names, nesting depth, file count, per-entry size, total size, and compression ratio.
- [x] 4.2 Harden log parsing and session storage with configured event, raw text, stack trace, summary, and response payload limits.
- [x] 4.3 Harden log search validation for keyword length, fragment count, filter lengths, time range consistency, levels, limits, and stack trace inclusion.
- [x] 4.4 Harden Evidence Pack, Codex task, OpenSpec draft, diagnosis report, and incident card generation with output bounds and sensitive-data masking.

## 5. Tests

- [x] 5.1 Add backend controller tests for malformed JSON, invalid path/query parameters, oversized strings, invalid enums, invalid URLs, and invalid numeric ranges.
- [x] 5.2 Add backend tests proving frontend-provided LLM API keys are protected locally and never returned as plaintext.
- [x] 5.3 Add log analysis tests for unsafe ZIP paths, nested archive limits, unsupported content types, excessive search filters, and oversized payloads.
- [x] 5.4 Add generated-artifact tests proving reports, evidence packs, Codex tasks, and OpenSpec drafts preserve masking and output caps.
- [x] 5.5 Run `mvn test` and update any frontend tests required by validation UX changes.

## 6. Documentation and Verification

- [x] 6.1 Update README or deployment documentation with security-related configuration values and production recommendations.
- [x] 6.2 Run `openspec status --change security-hardening-audit` and verify all implementation tasks are complete before archive.
