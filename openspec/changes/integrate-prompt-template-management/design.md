## Context

Thread Doctor now has multiple places that produce LLM or investigation-oriented text: diagnosis report prompts, Codex task Markdown, OpenSpec draft Markdown, and incident review style documents. The current implementation mixes prompt wording and rendering logic into services, which makes prompt changes difficult to review, test, override, or keep consistent across generated outputs.

This change introduces a prompt template management layer that treats prompts as versioned resources loaded from classpath defaults or an optional external directory. The implementation remains backend-only and does not add database persistence or a prompt editing UI.

## Goals / Non-Goals

**Goals:**

- Store default prompt templates under `src/main/resources/prompts/...`.
- Represent supported templates through typed metadata: template type, default path, content type, and description.
- Load prompt templates from an optional configured external directory first, then classpath resources.
- Render `{{variableName}}` and nested variables such as `{{incident.sessionId}}`.
- Support strict rendering that fails on unresolved variables and relaxed rendering that reports unresolved variables.
- Assemble diagnosis prompts, Codex investigation task prompts, OpenSpec change draft prompts, and incident review prompts through one shared prompt service.
- Keep JSON schema prompts loadable as JSON content and usable by the OpenAI-compatible REST client flow.
- Add tests for template loading, rendering, assembly, fallback, and generator integration.

**Non-Goals:**

- No frontend prompt management screen.
- No database-backed prompt storage or audit history.
- No runtime prompt editing API.
- No new LLM provider or model-selection feature.
- No automatic execution of generated Codex/OpenSpec/review documents.

## Decisions

### Typed template catalog

Create `PromptTemplateType` as the authoritative list of supported templates:

- `DIAGNOSIS_SYSTEM_PROMPT`
- `DIAGNOSIS_USER_PROMPT`
- `DIAGNOSIS_JSON_SCHEMA`
- `CODEX_INVESTIGATION_TASK`
- `OPENSPEC_CHANGE_DRAFT`
- `INCIDENT_REVIEW`

Each enum entry carries `templateId`, `defaultPath`, `contentType`, and description. This keeps service code from passing raw file paths around and gives tests a stable contract.

Alternative considered: string-based template IDs only. Rejected because call sites would be easier to mistype and harder to discover.

### Classpath defaults with external override

`PromptTemplateLoader` will load from `${thread-doctor.prompt.template-dir}` when configured and the target file exists. Otherwise it falls back to classpath resources. The loader returns source metadata so diagnostics can show whether a template came from `CLASSPATH` or `EXTERNAL_FILE`.

Alternative considered: external files only. Rejected because production startup should work with packaged defaults.

### Simple deterministic renderer

`PromptRenderer` will resolve `{{...}}` placeholders from a map/object variable context. It will support nested paths using dot notation, JSON-safe insertion for object values, strict unresolved-variable failures, and relaxed unresolved-variable reporting.

Alternative considered: adding a full templating engine dependency. Rejected for this change because the required syntax is small, deterministic, and easier to test without a broad dependency.

### Prompt assembly services

`PromptAssemblyService` will expose explicit methods:

- `buildDiagnosisPrompt(EvidencePack evidencePack, DiagnosisRequest request)`
- `buildCodexTaskPrompt(EvidencePack evidencePack)`
- `buildOpenSpecChangeDraftPrompt(EvidencePack evidencePack)`
- `buildIncidentReviewPrompt(EvidencePack evidencePack, DiagnosisReport diagnosisReport)`

Existing generators and diagnosis report code should use these methods instead of constructing prompt text inline. This keeps business services focused on gathering evidence and lets prompt rendering remain testable.

Alternative considered: inject `PromptRenderer` directly into every generator. Rejected because each generator would duplicate variable mapping and fallback behavior.

### Prompt-specific exceptions

Prompt errors will use domain exceptions such as `PromptTemplateNotFoundException`, `PromptTemplateLoadException`, `PromptRenderException`, and `MissingPromptVariableException`. Exceptions include template type, template path, missing variables where applicable, and a root cause message.

Alternative considered: use generic `IllegalArgumentException` / `IllegalStateException`. Rejected because prompt failures need actionable diagnostics during production debugging.

## Risks / Trade-offs

- Template drift can break diagnosis output -> strict rendering and tests cover required variables.
- External template path can be misconfigured -> classpath fallback is used only when the file is absent; unreadable or invalid files fail with a prompt-specific load error.
- Relaxed rendering can hide missing context -> strict rendering defaults to configured `true`; relaxed mode reports unresolved variables explicitly.
- JSON schema templates can become invalid JSON -> JSON content type is validated in tests and before use in diagnosis prompt assembly.
- Caching can serve stale external templates -> cache is configurable and can be disabled for local debugging.

## Migration Plan

1. Add prompt resources with current equivalent prompt text and JSON schema.
2. Add prompt config properties with safe defaults:
   - `thread-doctor.prompt.template-dir`
   - `thread-doctor.prompt.cache-enabled=true`
   - `thread-doctor.prompt.strict-rendering=true`
   - `thread-doctor.prompt.default-output-language=zh-CN`
3. Introduce prompt loader, renderer, assembly service, and exceptions.
4. Switch diagnosis and generated-artifact services to prompt assembly methods.
5. Run backend tests with classpath defaults and with a temporary external template directory.

Rollback removes the service wiring and returns generators to their prior inline prompt construction while keeping packaged prompt files harmless.

## Open Questions

- Whether external prompt files should be hot-reloaded when caching is disabled or only reloaded per request.
- Whether incident review output should stay behind an existing document API or receive its own endpoint in a future change.
