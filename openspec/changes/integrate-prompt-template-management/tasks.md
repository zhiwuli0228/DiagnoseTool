## 1. Prompt Resources and Configuration

- [x] 1.1 Add default prompt resource files under `src/main/resources/prompts/diagnosis`, `prompts/codex-task`, `prompts/openspec`, and `prompts/review`.
- [x] 1.2 Add `thread-doctor.prompt.*` configuration properties for template directory, cache enabled, strict rendering, and default output language.
- [x] 1.3 Add prompt content type and template source enums for Markdown, JSON, text, classpath, and external file sources.

## 2. Prompt Domain Model

- [x] 2.1 Add `PromptTemplateType` with template id, default path, content type, and description for all supported templates.
- [x] 2.2 Add prompt model records for `PromptTemplate`, `PromptRenderRequest`, `PromptRenderResult`, and `DiagnosisPrompt`.
- [x] 2.3 Add prompt-specific exceptions for not found, load failure, render failure, and missing variables.

## 3. Loading and Rendering

- [x] 3.1 Implement `PromptTemplateLoader` with external directory override, classpath fallback, source metadata, and configurable caching.
- [x] 3.2 Implement `PromptRenderer` for simple placeholders, nested dot-path variables, structured object JSON rendering, and deterministic unresolved-variable collection.
- [x] 3.3 Implement strict rendering failure and relaxed rendering result behavior.
- [x] 3.4 Validate JSON content templates when loading or assembling JSON schema prompt output.

## 4. Prompt Assembly

- [x] 4.1 Implement `PromptAssemblyService.buildDiagnosisPrompt(EvidencePack, DiagnosisRequest)` using diagnosis system, user, and JSON schema templates.
- [x] 4.2 Implement `buildCodexTaskPrompt(EvidencePack)` using the Codex investigation task template.
- [x] 4.3 Implement `buildOpenSpecChangeDraftPrompt(EvidencePack)` using the OpenSpec change draft template.
- [x] 4.4 Implement `buildIncidentReviewPrompt(EvidencePack, DiagnosisReport)` using the incident review template.

## 5. Existing Flow Integration

- [x] 5.1 Update diagnosis report generation so LLM requests use `DiagnosisPrompt` from the prompt assembly service.
- [x] 5.2 Update Codex task generation to render through `CODEX_INVESTIGATION_TASK`.
- [x] 5.3 Update OpenSpec change draft generation to render through `OPENSPEC_CHANGE_DRAFT`.
- [x] 5.4 Update incident review or incident-card style document generation to render through `INCIDENT_REVIEW` when that document type is requested.
- [x] 5.5 Preserve existing masking, output bounds, document-only behavior, and no-auto-execution guarantees.

## 6. Tests and Verification

- [x] 6.1 Add tests for classpath prompt loading and external directory override behavior.
- [x] 6.2 Add tests for cache-enabled and cache-disabled template loading behavior.
- [x] 6.3 Add tests for simple variables, nested variables, structured JSON variables, strict failures, and relaxed unresolved-variable reporting.
- [x] 6.4 Add tests for diagnosis prompt assembly including system prompt, user prompt, and JSON schema.
- [x] 6.5 Add tests for Codex task, OpenSpec draft, and incident review prompt rendering.
- [x] 6.6 Add integration or service tests proving existing generators use the prompt assembly layer.
- [x] 6.7 Run `mvn test` and fix failures within the change scope.
