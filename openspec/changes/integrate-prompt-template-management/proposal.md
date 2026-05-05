## Why

Thread Doctor currently keeps diagnosis and generated-document prompt text inside application code, making prompt changes hard to review, test, reuse, and override per environment. This change externalizes prompt templates and introduces a typed loading/rendering/assembly layer so diagnosis, Codex task generation, OpenSpec draft generation, and incident review documents can share consistent, testable prompt behavior.

## What Changes

- Add classpath prompt resources under `src/main/resources/prompts/...` for diagnosis, Codex task generation, OpenSpec draft generation, and incident review.
- Add typed prompt template metadata for supported template types, default paths, content types, and descriptions.
- Add prompt template loading from classpath resources with optional external directory override via configuration.
- Add prompt rendering for `{{variableName}}` placeholders, nested map/object variables such as `{{incident.sessionId}}`, strict and relaxed unresolved-variable behavior, and JSON-safe variable rendering.
- Add prompt assembly services for diagnosis prompts, Codex task prompts, OpenSpec draft prompts, and incident review prompts.
- Update existing diagnosis and generated-artifact flows to use the prompt assembly layer instead of hard-coded prompt strings.
- Add prompt-specific exceptions with template type, path, missing variables, and root-cause messages.
- Add configuration for template directory, caching, strict rendering, and default output language.
- Add focused JUnit 5 tests for loading, rendering, prompt assembly, integration with existing generators, configuration fallback, and error handling.

## Capabilities

### New Capabilities

- `prompt-template-management`: Covers prompt template resources, typed metadata, loading, rendering, assembly, configuration, caching behavior, and prompt-specific error handling.

### Modified Capabilities

- `diagnosis-report`: Diagnosis report generation SHALL obtain its system prompt, user prompt, and JSON schema through the prompt template management layer.
- `codex-investigation-artifacts`: Codex task and OpenSpec draft generation SHALL render configured prompt templates instead of assembling fixed Markdown directly in service code.
- `incident-card-memory`: Incident review or incident-card style generated documents SHALL render through the configured incident review template when that document type is requested.

## Impact

- Adds resources under `src/main/resources/prompts/...`.
- Adds backend prompt domain, loader, renderer, assembly service, configuration properties, and exceptions.
- Updates diagnosis prompt construction and generated artifact services to depend on prompt assembly.
- Adds tests for prompt rendering behavior, classpath/external fallback, strict vs relaxed rendering, JSON schema loading, and generator integration.
- No database persistence, frontend workflow expansion, or automatic execution of generated prompts is introduced.
