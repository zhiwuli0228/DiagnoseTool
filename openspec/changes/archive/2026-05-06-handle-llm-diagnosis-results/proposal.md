## Why

The current diagnosis flow can ask an LLM to summarize supplied evidence, but the model does not have codebase access. When evidence is insufficient for a confident root-cause decision, the product should guide the user to either provide more key information or hand off a focused prompt to Codex/OpenCode for codebase-aware investigation.

## What Changes

- Add diagnosis result assessment that classifies whether the current evidence and LLM output are sufficient to locate the problem.
- Represent ambiguous or unresolved diagnosis results explicitly instead of treating every LLM response as final.
- When the problem cannot be clearly located, return next-step options for the frontend:
  - request specific additional evidence from the user with clear requirements;
  - generate a bounded codebase investigation prompt suitable for Codex/OpenCode.
- Add frontend controls that let the user choose to continue diagnosis by submitting more evidence or copy the generated codebase prompt.
- Reuse existing prompt template and artifact safety rules so generated prompts are bounded, masked, and evidence-based.

## Capabilities

### New Capabilities
- `llm-diagnosis-result-handling`: Decision flow for interpreting LLM diagnosis results, requesting more evidence, and producing codebase investigation prompts when direct localization is not possible.

### Modified Capabilities
- `diagnosis-report`: Diagnosis reports must expose localization status, unresolved reasons, required follow-up evidence, and optional codebase investigation prompt metadata.
- `conversational-diagnosis-frontend`: The frontend must present unresolved diagnosis choices and allow the user to continue evidence collection or copy a codebase prompt.
- `codex-investigation-artifacts`: Generated codebase prompts must support diagnosis-report handoff, not only log-analysis Evidence Pack sessions.

## Impact

- Backend diagnosis report domain model, diagnosis service, prompt assembly, and LLM response validation.
- Backend API response shape for diagnosis reports.
- Frontend workflow state, report rendering, buttons, copy behavior, and tests.
- Prompt templates for diagnosis output and Codex/OpenCode handoff.
- Tests for resolved diagnosis, unresolved diagnosis, follow-up evidence requests, and generated codebase prompt output.
