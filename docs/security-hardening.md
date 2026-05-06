# Security Hardening Notes

## Frontend-Facing Inputs

- Incident APIs: `sessionId`, incident title, description, severity, evidence type, evidence source, evidence content, and evidence metadata.
- Metrics APIs: `sessionId` plus JVM, Redis, Kafka, and DB metrics JSON snippets.
- Diagnosis, recovery, and incident card APIs: `sessionId`, `actionId`, and generated artifact responses.
- LLM configuration APIs: runtime `baseUrl` and `model`; API keys are read only from `LLM_API_KEY`.
- Log analysis APIs: log session IDs, ZIP upload, directory file upload, directory scan path, evidence pack format, and log search filters.

## Bounds

- IDs use endpoint-specific prefixes and bounded alphanumeric formats such as `INC-*`, `LOG-*`, and `ACT-*`.
- Text fields are bounded at the API boundary: titles, descriptions, evidence, metadata, metrics JSON, LLM settings, directory paths, search filters, and generated artifacts.
- ZIP and directory log ingestion enforce file count, compressed size, uncompressed size, per-entry size, nesting depth, supported suffixes, path traversal protection, and compression ratio limits.
- Log search validates time ranges, levels, keyword length, keyword fragment count, field lengths, and result limits before scanning cached events.

## Rejection vs Truncation

- Invalid IDs, unsafe paths, invalid URLs, invalid enum values, malformed limits, unsupported upload types, and oversized request inputs are rejected with structured `400` errors.
- Derived text outputs may be truncated only where the output contract already supports bounded excerpts; truncation keeps the existing `[truncated]` marker.

## Sensitive Data

- LLM API keys must be provided through the `LLM_API_KEY` environment variable and are not accepted from frontend requests or application YAML files.
- Configuration status responses expose only masked environment key state, never plaintext API keys.
- API errors are sanitized before being returned.
- Logs and generated artifacts mask emails, IPs, phone-like values, API keys, tokens, passwords, authorization headers, cookies, and equivalent credential-like fields.

## Production Configuration

- Set `LLM_API_KEY` in the runtime environment before enabling real LLM calls in production.
- Keep log analysis limits conservative unless the JVM heap and expected upload sizes have been tested.
- Do not disable masking in production.
