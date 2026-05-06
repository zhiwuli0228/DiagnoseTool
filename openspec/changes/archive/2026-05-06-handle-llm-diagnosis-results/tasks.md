## 1. Backend Domain Model

- [x] 1.1 Add diagnosis localization status values such as `LOCALIZED`, `UNRESOLVED`, and `NEEDS_MORE_EVIDENCE`.
- [x] 1.2 Extend diagnosis report response data with localization status, unresolved reasons, follow-up evidence requests, and optional codebase prompt fields.
- [x] 1.3 Add a bounded data model for follow-up evidence requests with title, reason, expected format, and guidance.
- [x] 1.4 Add a bounded data model for codebase investigation prompt metadata and Markdown content.

## 2. Diagnosis Result Handling

- [x] 2.1 Update diagnosis prompt/schema expectations so LLM output includes localization and next-action fields.
- [x] 2.2 Validate LLM diagnosis output before presenting a final localized report.
- [x] 2.3 Add fallback handling that converts incomplete or ambiguous LLM output into unresolved diagnosis metadata.
- [x] 2.4 Generate specific follow-up evidence requests when missing evidence prevents safe localization.

## 3. Codebase Prompt Generation

- [x] 3.1 Add or update a prompt template for Codex/OpenCode codebase investigation based on unresolved diagnosis reports.
- [x] 3.2 Implement backend codebase prompt generation from diagnosis context, evidence, unresolved reasons, and suspected areas.
- [x] 3.3 Apply existing sensitive-data masking, output bounds, and document-only safety rules to generated prompts.
- [x] 3.4 Ensure generated prompts do not execute commands, read source files, mutate repository files, or call external codebase tools.

## 4. API and Frontend Flow

- [x] 4.1 Ensure diagnosis API responses expose unresolved diagnosis metadata and codebase prompt content when applicable.
- [x] 4.2 Update frontend workflow state to track localization status, unresolved reasons, follow-up evidence requests, and codebase prompt content.
- [x] 4.3 Render unresolved diagnosis choices in the frontend: continue providing key information or copy codebase prompt.
- [x] 4.4 Let users submit requested follow-up evidence and re-run diagnosis without losing existing evidence.
- [x] 4.5 Add copy behavior for the codebase investigation prompt without invoking Codex/OpenCode automatically.

## 5. Tests

- [x] 5.1 Add backend unit tests for localized, unresolved, and needs-more-evidence diagnosis result handling.
- [x] 5.2 Add backend tests for incomplete LLM output fallback behavior.
- [x] 5.3 Add backend tests for codebase prompt masking, bounds, and document-only content.
- [x] 5.4 Add frontend tests for unresolved diagnosis rendering and follow-up evidence submission.
- [x] 5.5 Add frontend tests for codebase prompt rendering and copy behavior.
- [x] 5.6 Run `mvn test` and `npm.cmd test`.

## 6. Documentation and Verification

- [x] 6.1 Update README or relevant docs to describe unresolved diagnosis handling and Codex/OpenCode prompt handoff.
- [x] 6.2 Run `openspec status --change handle-llm-diagnosis-results` and verify all artifacts and tasks are ready before implementation completion.
