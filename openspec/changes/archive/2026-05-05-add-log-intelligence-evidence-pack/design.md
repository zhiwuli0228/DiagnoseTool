## Context

Thread Doctor can guide diagnosis once useful evidence exists, but production logs are usually delivered as ZIP bundles or server-side files that require manual filtering before they are useful. The new capability creates a bounded cache-backed log analysis session that extracts enough structured evidence for diagnosis and code investigation while avoiding full codebase upload or persistent storage.

The source requirement is `docs/design-change.md`. The capability now includes the React frontend entry required to operate the backend APIs from the product UI. It does not introduce external log platform integration, database tables, or automatic execution of generated Codex/OpenSpec work.

## Goals / Non-Goals

**Goals:**

- Accept logs from ZIP upload, browser-selected local directory upload, and configured local directory scan sources.
- Normalize logs into structured events with Java stack trace and trace ID extraction.
- Make malformed input visible through structured `UNPARSED` records.
- Support bounded search, clustering, timeline, evidence extraction, suspected code area extraction, Evidence Pack JSON/Markdown output, Codex task generation, and OpenSpec draft generation.
- Provide a React UI that lets the user choose either ZIP upload or local directory upload, then populate the existing log snippet evidence input after bounded keyword extraction, with status/summary display before the user submits evidence to the active diagnosis session.
- Protect production usage with feature switches, size limits, file count limits, directory allowlists, output caps, decompression safeguards, and sensitive data masking.
- Keep all derived state cache-backed and avoid database persistence.

**Non-Goals:**

- No Elasticsearch, Loki, Splunk, or observability platform integration.
- No real codebase scanning or source code reading.
- No execution of Codex tasks, scripts, build commands, or generated OpenSpec changes.
- No automatic submission or modification of repository OpenSpec changes from generated drafts.
- No distributed log collection agent or streaming ingestion pipeline.

## Decisions

### Cache-backed session model

The system will create `LogAnalysisSession` records in cache and store parsed events plus derived summaries under the session ID. This keeps the capability aligned with the current diagnosis workflow requirement that analysis data remains transient unless the user exports a result document.

Alternative considered: database persistence. It was rejected because the requested capability is evidence preparation and export, not long-term log storage.

### Source abstraction with production guardrails

ZIP upload, browser-selected directory upload, and configured server-side directory scan will share a `LogSource` abstraction so parsing, masking, and downstream analysis are independent from source type. Directory scan will only read paths under configured allowlist roots and can be disabled by configuration for production. Browser-selected directory upload preserves relative file paths from the frontend and still applies file count, size, nested archive, and output limits.

Alternative considered: unrestricted directory scan for easier local debugging. It was rejected because production deployments need explicit filesystem boundaries.

### Secure bounded ZIP ingestion

ZIP processing will reject path traversal entries, excessive file counts, excessive compressed or uncompressed size, and unsafe compression ratios. The parser will only retain bounded raw excerpts and stack traces so API responses cannot become unbounded log dumps.

Alternative considered: parse the entire archive into memory. It was rejected because large incident bundles can create memory pressure and slow responses.

### Parser pipeline

Parsing will use a staged pipeline: line classification, multi-line Java stack trace folding, trace ID extraction, structured event creation, and fallback `UNPARSED` event creation. Supported trace ID patterns include `traceId=xxx`, `trace_id=xxx`, `traceId: xxx`, `[traceId:xxx]`, and `X-B3-TraceId=xxx`.

Alternative considered: only parse one known application log format. It was rejected because production bundles often contain mixed formats and partial lines.

### Mask before output

Sensitive values will be masked before storage in derived outputs and before API responses. The masking layer will target IPs, emails, phone-like values, tokens, passwords, secrets, keys, and similar credential fields while preserving enough structure for diagnosis.

Alternative considered: expose raw logs and let users manage masking. It was rejected because the APIs are designed for AI-ready evidence and must be safe by default.

### Deterministic evidence generation

Clustering, timeline, evidence extraction, suspected code area extraction, Codex task generation, and OpenSpec draft generation will be deterministic template and heuristic logic. LLM calls are not required for this change.

Alternative considered: ask an LLM to classify evidence directly. It was rejected because the requested output can be produced from logs deterministically and needs predictable tests.

### Frontend log snippet integration

The React app will expose ZIP upload and local directory selection under the existing log snippet evidence input. The two source modes are mutually exclusive so the user cannot accidentally submit both a ZIP and a directory in the same parse action. Upload creates a parsed log session and displays bounded summaries, clusters, and timeline hints, but it does not write analysis output into the `LOG_SNIPPET` draft automatically.

Users must enter one or more problem-specific keywords such as trace IDs, exception names, order IDs, endpoint names, or high-risk terms and run bounded search before extraction. Keyword input supports multiple tokens, optional log levels, optional time range, stack trace inclusion, ignore-case preference, and duplicate compression. Only the matched event subset is formatted into the `LOG_SNIPPET` draft. When duplicate compression is enabled, identical rows are represented once with an occurrence count. This keeps 3-5 GB log bundles from becoming direct LLM input and makes token usage proportional to user-selected evidence.

For large bundles, the frontend will show only the first few file summaries plus a displayed/total count. Cluster rows will prefer exception type, sample log, suspected class, or fingerprint before falling back to the internal cluster ID. Timeline rows will keep severity labels in a fixed column and clamp long summaries so content cannot stretch the panel.

ZIP ingestion will recursively inspect ZIP entries whose names and headers indicate nested ZIP archives, such as `collect_xxx.log.zip`. Nested entries share the same file count, uncompressed size, compression ratio, and ZIP slip safeguards as top-level entries.

Generated Codex and OpenSpec artifacts remain backend document APIs, but the primary frontend behavior is evidence preparation rather than a standalone investigation panel. The UI will use the same API client pattern, rate limiting guard, Chinese i18n model, and tips on key controls already used by diagnosis workflows.

Alternative considered: leave the capability as API-only and document curl usage. It was rejected because project capabilities are expected to be usable from the frontend.

## Risks / Trade-offs

- Mixed log formats may reduce parse accuracy -> Preserve `UNPARSED` events and include limitations in the Evidence Pack.
- Masking can hide useful evidence -> Mask only sensitive values and keep stable placeholders that preserve shape.
- Directory scanning can expose server files -> Require feature switch and configured allowlist roots.
- Large archives can cause memory pressure -> Enforce file count, size, ratio, per-event excerpt, sample, and response limits.
- Fingerprint clustering can merge unrelated errors -> Include sample events, source files, trace IDs, and limitations so engineers can verify clusters.
- Frontend directory uploads can contain many irrelevant files -> Browser-selected directory upload uses the same configured limits and still requires user-driven bounded search before evidence submission.

## Migration Plan

No database migration is required. The change can be deployed behind configuration switches for ZIP ingestion and directory scan. Rollback disables the feature switches or removes the new API routes without affecting existing diagnosis flows.

## Open Questions

- Which exact local directory roots should be enabled for production deployments?
- What default size and file count limits should be used for the first release?
- Which log timestamp formats are mandatory for the first implementation beyond common Java application logs?
