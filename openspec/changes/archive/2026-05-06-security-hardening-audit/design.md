## Context

Thread Doctor exposes several frontend-driven workflows: incident sessions, evidence submission, metrics snapshots, log ZIP upload, log directory upload, log search, generated artifacts, prompt rendering, and runtime LLM configuration. Some flows already have partial safeguards, especially log ZIP limits and masked LLM status responses, but the security posture is not yet expressed as a cross-cutting contract.

The application intentionally uses local cache rather than a database. LLM API keys must not be accepted from frontend requests or stored in YAML; they must come from the `LLM_API_KEY` environment variable and must never be echoed back to the frontend.

## Goals / Non-Goals

**Goals:**
- Validate every frontend-originated input at the API boundary and repeat critical checks inside services that handle expensive operations.
- Bound request sizes, string lengths, collection sizes, file sizes, decompression ratios, search limits, and generated output sizes.
- Encrypt locally retained secrets and redact sensitive values before responses, logs, reports, and generated artifacts.
- Return consistent validation and safety errors without exposing rejected secret values.
- Add tests for malformed input, oversized input, unsafe paths, archive abuse, secret encryption, and non-disclosure.

**Non-Goals:**
- Add authentication, authorization, RBAC, SSO, or multi-tenant isolation.
- Introduce a database, external secret manager, Elasticsearch, Loki, Splunk, WAF, or gateway service.
- Replace the log analysis architecture with a full indexing engine.
- Guarantee irreversible anonymization of all uploaded business log content.

## Decisions

1. Use Bean Validation plus service-level guards.

   Controller DTOs should use `@Valid`, length/range annotations, enum validation, and URL/path constraints for predictable rejection. Service-level checks remain required for expensive operations such as ZIP parsing, directory scans, search, and generated artifact creation because these operations can be invoked from multiple paths.

   Alternative considered: frontend-only validation. This is insufficient because attackers can call backend APIs directly.

2. Centralize API safety errors.

   Validation failures, unsafe archive failures, size-limit failures, and unsupported input failures should be returned through a consistent error shape via the existing exception handler. Error messages must name the failed field or boundary without including raw API keys, tokens, uploaded log fragments, or full filesystem paths unless explicitly safe.

   Alternative considered: ad hoc controller responses. This would make it harder to verify no secret leaks.

3. Source LLM API keys only from environment variables.

   Frontend-provided API keys must be rejected. The effective LLM client reads the API key from backend environment configuration via `LLM_API_KEY`; read/status APIs return only source metadata and masked state, never plaintext.

   Alternative considered: encrypt frontend-provided API keys locally. This still creates an unnecessary key-retention path and conflicts with the environment-only requirement.

4. Keep log handling bounded rather than adding external indexing.

   This change should harden current log handling with stricter upload, entry, event, search, and output limits. Performance improvements such as streaming parsing or indexes can be proposed separately unless needed to enforce safety boundaries.

   Alternative considered: introduce a search index now. That would expand scope beyond security hardening.

5. Prefer deny-by-default constraints.

   Unknown enum values, unsupported content types, unsafe paths, blank identifiers, invalid URLs, overly broad search requests, and excessive limits should be rejected or reduced to configured safe maximums.

   Alternative considered: permissive parsing with best-effort fallback. That increases DoS and injection risk.

## Risks / Trade-offs

- Existing frontend workflows may submit values that were previously accepted but are now rejected -> Add frontend-side validation hints and regression tests for normal flows.
- Missing `LLM_API_KEY` will prevent real LLM calls -> Return a clear backend configuration error and document the required environment variable.
- Redaction can remove useful diagnostic detail -> Keep structured metadata such as “configured=true” and bounded masked fingerprints while withholding plaintext.
- Strict log limits may reject legitimate large uploads -> Make limits configurable, return actionable structured errors, and keep generated reports explicit about truncated or rejected content.
- Validation annotations can miss nested or non-JSON inputs -> Add explicit service tests for multipart files, ZIP entries, directory file names, and generated output paths.
