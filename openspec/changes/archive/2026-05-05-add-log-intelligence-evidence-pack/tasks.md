## 1. API and Session Foundation

- [x] 1.1 Add cache-backed `LogAnalysisSession`, `LogSource`, `LogFileSummary`, and configuration models for feature switches, limits, output caps, masking, and directory allowlists.
- [x] 1.2 Add `/api/log-analysis/sessions` APIs for session creation, session lookup, ZIP upload, and directory scan requests.
- [x] 1.3 Add structured error responses for disabled features, unsafe paths, unsupported sources, oversized inputs, and parsing failures.

## 2. Source Ingestion and Safety

- [x] 2.1 Implement ZIP ingestion with ZIP slip rejection, file count limits, compressed/uncompressed size limits, and decompression ratio safeguards.
- [x] 2.2 Implement local directory scan ingestion behind a configuration switch and configured allowlist roots.
- [x] 2.3 Add bounded raw text, stack trace, sample log, and response payload caps.
- [x] 2.4 Add sensitive data masking for IPs, emails, phone-like values, passwords, tokens, secrets, keys, and similar credential fields.

## 3. Log Parsing and Search

- [x] 3.1 Implement log parsing into `LogEvent` records with timestamp, level, thread name, logger name, trace ID, message, exception type, stack trace, raw text, source file, line number, and tags.
- [x] 3.2 Implement Java stack trace folding for `at`, `Caused by`, `Suppressed`, and common frames omitted lines.
- [x] 3.3 Preserve unsupported or malformed lines as `UNPARSED` events.
- [x] 3.4 Implement bounded log search with filters for time range, levels, keywords, trace ID, thread name, logger name, exception type, source file, limit, and stack trace inclusion.

## 4. Analysis Outputs

- [x] 4.1 Implement exception fingerprint clustering with volatile token normalization and sorted `LogCluster` output.
- [x] 4.2 Implement incident timeline generation from important warnings, errors, repeated failures, high-risk exceptions, keywords, and trace-linked events.
- [x] 4.3 Implement evidence extraction into bounded `EvidenceItem` records.
- [x] 4.4 Implement suspected code area extraction from stack traces and logger names, preferring business classes over excluded framework packages.
- [x] 4.5 Implement Evidence Pack JSON and Markdown generation with source summaries, file summaries, incident summary, clusters, timeline, evidence, suspected areas, recommended questions, recommended checks, and limitations.

## 5. Generated Investigation Artifacts

- [x] 5.1 Implement Codex task Markdown generation from Evidence Pack with required investigation sections and engineering constraints.
- [x] 5.2 Implement OpenSpec change draft generation from Evidence Pack as downloadable draft content only.
- [x] 5.3 Ensure generated artifacts are masked, bounded, and never auto-executed or auto-submitted to the repository.

## 6. Tests and Verification

- [x] 6.1 Add JUnit 5 and Mockito tests for ZIP upload parsing, ZIP slip rejection, size limits, file count limits, and directory allowlist behavior.
- [x] 6.2 Add tests for malformed log visibility, Java stack trace parsing, trace ID extraction, search filters, and stack trace inclusion behavior.
- [x] 6.3 Add tests for exception fingerprint clustering, suspected code area extraction, timeline sorting/linking, Evidence Pack generation, Codex task generation, and OpenSpec draft generation.
- [x] 6.4 Add tests for sensitive data masking and output length caps.
- [x] 6.5 Run `mvn test` and fix failures within the change scope.

## 7. Frontend Log Analysis Entry

- [x] 7.1 Add frontend API client methods for log analysis session creation, ZIP upload, directory scan, clusters, timeline, Evidence Pack, Codex task, and OpenSpec draft requests.
- [x] 7.2 Add React UI controls for ZIP upload and directory scan with localized labels and clear tips.
- [x] 7.3 Render log analysis session status, file summaries, clusters, and timeline with bounded layout.
- [x] 7.4 Add frontend unit tests for ZIP upload, directory scan, result rendering, log snippet population, and key tips.
- [x] 7.5 Run frontend tests and build, then update task status.

## 8. Frontend Log Snippet Integration

- [x] 8.1 Move ZIP upload and directory scan controls into the existing log snippet evidence input area instead of a standalone log analysis panel.
- [x] 8.2 Convert parsed log analysis output into bounded `LOG_SNIPPET` draft content so diagnosis uses the parsed result through the normal evidence submission flow.
- [x] 8.3 Keep parsed file summaries, clusters, and timeline visible as supporting context under the log snippet input.
- [x] 8.4 Update frontend tests for ZIP upload and directory scan as first-class log snippet inputs.
- [x] 8.5 Run frontend tests, frontend build, OpenSpec validation, and backend tests, then update task status.

## 9. Bounded Keyword Extraction for Large Logs

- [x] 9.1 Add frontend API client support for bounded log search.
- [x] 9.2 Change ZIP upload and directory scan so they prepare a log analysis session but do not auto-fill `LOG_SNIPPET`.
- [x] 9.3 Add keyword, level, limit, and stack trace controls for extracting relevant log events into the log snippet draft.
- [x] 9.4 Update frontend tests so large log sources require keyword extraction before evidence submission.
- [x] 9.5 Run frontend tests, frontend build, OpenSpec validation, and backend tests, then update task status.

## 10. Nested Archive and Readable Summary UX

- [x] 10.1 Limit frontend log file summary rendering and show a concise displayed/total count.
- [x] 10.2 Replace opaque cluster IDs in the frontend with exception, sample log, or suspected class labels.
- [x] 10.3 Constrain timeline row layout so long content does not stretch severity labels or break the panel.
- [x] 10.4 Parse nested ZIP log archives inside uploaded ZIP bundles while preserving existing ZIP safety limits.
- [x] 10.5 Add focused frontend and backend tests, then run frontend tests, frontend build, OpenSpec validation, and backend tests.

## 11. Robust Log Search Matching

- [x] 11.1 Remove frontend default level filtering so keyword searches can find INFO/DEBUG trace and business lines unless the user explicitly filters levels.
- [x] 11.2 Expand backend keyword search to include raw text, trace ID, thread name, source file, and tags.
- [x] 11.3 Support split keyword matching for line-break separated input while preserving punctuation inside each fragment.
- [x] 11.4 Add backend and frontend regression tests for trace/source/raw-text keyword search.
- [x] 11.5 Run frontend tests, frontend build, OpenSpec validation, and backend tests, then update task status.

## 12. Frontend Source Choice and Advanced Search Filters

- [x] 12.1 Add mutually exclusive frontend controls for ZIP upload and local directory selection, with directory files uploaded using preserved relative paths.
- [x] 12.2 Add optional frontend log level selection, multi-keyword input, ignore-case toggle, and optional time range filters.
- [x] 12.3 Add backend directory file upload ingestion and search support for ignore-case and time filters without expanding persistence scope.
- [x] 12.4 Add focused frontend and backend regression tests for directory upload, level filters, multi-keyword search, ignore-case, and time range filtering.
- [x] 12.5 Run frontend tests, frontend build, OpenSpec validation, and backend tests, then update task status.

## 13. Log Search Form Usability

- [x] 13.1 Move the keyword input to a dedicated row so it is not squeezed by filters.
- [x] 13.2 Change log level selection from inline checkboxes to a dropdown with an empty all-level option.
- [x] 13.3 Keep start and end time filters optional and editable with native date-time inputs.
- [x] 13.4 Update frontend regression tests and run frontend verification.

## 14. Deduplicated Log Search UX

- [x] 14.1 Split the second search filter line into multiple rows so controls do not squeeze each other.
- [x] 14.2 Add a frontend-controlled deduplication option for compressing identical log search results.
- [x] 14.3 Return duplicate counts for compressed log rows and include them in extracted snippets.
- [x] 14.4 Add focused frontend and backend tests, then run verification.

## 15. Visible Log Search Guidance

- [x] 15.1 Show visible frontend guidance for multi-keyword input separators.
- [x] 15.2 Show visible guidance for log source actions, filter inputs, toggle options, search, and extraction actions.
- [x] 15.3 Update frontend tests and run verification.

## 16. Safe Multi-fragment Keyword Matching

- [x] 16.1 Change multi-fragment keyword matching to use one fragment per line instead of splitting on spaces or punctuation.
- [x] 16.2 Update visible frontend guidance so punctuation inside a log fragment is preserved for matching.
- [x] 16.3 Add backend regression coverage for punctuation-preserving keyword phrases.

## 17. Diagnosis Page Column Order

- [x] 17.1 Move the operation panels to the left column and the conversation/log panel to the right column.
- [x] 17.2 Keep mobile layout as a single natural-flow column.
