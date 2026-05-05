## Why

Thread Doctor already has diagnosis workflows, but production incidents still require engineers to manually read large log bundles and extract evidence before asking AI or Codex for codebase investigation. This change adds a log intelligence layer that turns uploaded or locally scanned logs into bounded, masked, structured evidence without uploading the entire codebase to AI.

## What Changes

- Add log analysis sessions backed by cache, with sources from ZIP uploads, browser-selected local directory uploads, and configured local directory scans.
- Parse log files into structured `LogEvent` records, including timestamps, levels, threads, loggers, trace IDs, messages, Java stack traces, raw excerpts, source files, line numbers, and tags.
- Preserve malformed or unsupported log lines as visible `UNPARSED` records instead of silently dropping them.
- Provide bounded search/filter APIs over parsed log events by time range, level, multiple keywords, ignore-case preference, duplicate compression preference, trace ID, thread, logger, exception type, source file, and stack trace inclusion.
- Cluster similar logs by fingerprints derived from exception type, normalized messages, and top stack frames.
- Build incident timelines from error/warning events, high-risk keywords, repeated failures, and trace-linked log chains.
- Extract evidence items and suspected code areas, preferring business classes over common framework packages.
- Generate an Evidence Pack as JSON and Markdown, including summaries, clusters, timeline, evidence, suspected code areas, recommended Codex questions, recommended checks, and limitations.
- Generate bounded Markdown drafts for Codex investigation tasks and OpenSpec change proposals from the Evidence Pack as optional exported documents.
- Integrate mutually exclusive ZIP log bundle upload and browser-selected local directory upload into the existing React evidence collection area, using parsed key log findings as first-class `LOG_SNIPPET` input for the active diagnosis workflow.
- Add a frontend keyword extraction step after ZIP or directory parsing so large 3-5 GB log sources are searched and reduced before any log snippet is submitted to diagnosis.
- Improve large ZIP usability by limiting file summary rendering, replacing opaque cluster IDs with meaningful labels, constraining timeline rows, and parsing nested log ZIP archives inside uploaded bundles.
- Enforce ZIP slip protection, archive size/file count limits, decompression safety limits, output length caps, and sensitive data masking.

## Capabilities

### New Capabilities

- `log-intelligence-evidence-pack`: Covers log analysis sessions, source ingestion, log parsing, event search, clustering, timeline generation, evidence extraction, suspected code area extraction, and Evidence Pack output.
- `codex-investigation-artifacts`: Covers generation of Codex investigation task Markdown and OpenSpec change draft artifacts from an Evidence Pack.

### Modified Capabilities

- None.

## Impact

- Adds backend API surface under `/api/log-analysis/sessions`.
- Adds React frontend UI and API client methods for log analysis operations inside the existing evidence collection workflow.
- Adds cache-backed session state and derived analysis models; no database persistence is introduced.
- Adds log parsing, masking, clustering, evidence extraction, and artifact generation services.
- Adds production safety configuration for upload limits, directory scan roots, output caps, masking behavior, and feature switches.
- Adds JUnit 5 and Mockito coverage for ZIP ingestion, parsing, searching, clustering, timeline generation, evidence generation, artifact generation, security limits, masking, and malformed log visibility.
- Adds frontend unit tests for ZIP upload, directory scan, first-class log snippet population, result rendering, and user tips.
- Adds frontend unit tests that verify large log sources are not auto-filled into diagnosis evidence until the user performs bounded keyword extraction.
- Adds frontend and backend regression tests for directory upload, multi-keyword search, optional level filters, ignore-case behavior, and optional time range filtering.
