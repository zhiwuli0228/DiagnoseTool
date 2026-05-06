## Context

Thread Doctor currently builds diagnosis reports from supplied evidence and LLM output. The LLM can reason over provided logs, jstack, metrics, and user notes, but it cannot inspect the codebase. When evidence is incomplete or points only to a suspicious area, returning a final-looking report can mislead users into assuming the issue is fully located.

The project already has prompt-template management, diagnosis progress, Codex task generation from log Evidence Packs, and a conversational frontend. This change connects those pieces at the diagnosis-report level: after LLM processing, the system must decide whether the issue is located, needs more evidence, or should be handed to a codebase-aware tool.

## Goals / Non-Goals

**Goals:**
- Add an explicit diagnosis localization decision to diagnosis results.
- Support unresolved diagnosis outcomes with clear reasons and confidence.
- Provide follow-up evidence requests that users can submit to continue diagnosis.
- Provide a bounded, masked Codex/OpenCode prompt for codebase investigation when direct localization is not possible.
- Expose frontend choices without automatically invoking Codex/OpenCode or reading source code.

**Non-Goals:**
- Add actual codebase scanning inside Thread Doctor.
- Automatically run Codex/OpenCode, execute commands, or modify repository files.
- Replace existing diagnosis report or Evidence Pack generation.
- Require a new database or durable persistence layer.

## Decisions

1. Add structured result handling instead of parsing report text.

   Diagnosis should expose structured fields such as `localizationStatus`, `unresolvedReason`, `followUpQuestions`, and `codebasePrompt`. This avoids brittle frontend logic that infers next actions from natural language.

   Alternative considered: detect phrases such as “cannot determine” in the report summary. This is unreliable across models and languages.

2. Keep codebase prompt generation document-only.

   The generated prompt should be returned as Markdown for the user to copy into Codex/OpenCode. Thread Doctor must not execute it or inspect source code.

   Alternative considered: calling Codex automatically. That would expand the product boundary and require tool orchestration, permissions, and repository mutation controls.

3. Allow the user to continue diagnosis by submitting targeted evidence.

   When more evidence is needed, the backend should return specific required inputs, such as exact logs around a timestamp, jstack snapshots, config snippets, metrics windows, trace IDs, or exception stack frames. The frontend should keep existing evidence and let the user submit the requested data before re-running diagnosis.

   Alternative considered: returning only a generic “need more information” message. That does not help users improve evidence quality.

4. Reuse prompt templates and artifact safety.

   Codebase investigation prompts should use the existing template-loading and masking/limit infrastructure where possible, with a diagnosis-report specific template or assembly path.

   Alternative considered: hard-code prompt Markdown in the controller. This would diverge from the prompt management capability.

## Risks / Trade-offs

- Model output may omit the new structured fields -> Validate LLM response and fall back to unresolved status with a generated evidence request.
- Prompt could include too much raw evidence -> Apply existing masking and output bounds before returning it.
- Users may skip follow-up evidence and copy a weak codebase prompt -> Include unresolved reasons and explicit evidence limitations in the prompt.
- Frontend complexity may increase -> Keep the UI to two explicit actions: provide more information or copy codebase prompt.
