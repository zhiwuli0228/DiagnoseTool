## Why

The project has accumulated backend diagnosis, frontend workflow, log analysis, prompt management, deployment, and security capabilities, but lacks a single design overview that helps maintainers understand the whole system quickly.

## What Changes

- Add `readme-design.md` at the project root.
- Summarize the current project capabilities and core workflows.
- Include Mermaid class diagrams, sequence diagrams, and activity diagrams.
- Document core modules, major data flow, deployment/runtime shape, and safety boundaries.
- Keep the document concise enough for onboarding while still covering the full system.

## Capabilities

### New Capabilities
- `project-design-documentation`: Defines the required project-level design documentation for architecture summary, diagrams, workflows, and core capability introduction.

### Modified Capabilities

None.

## Impact

- Adds root documentation file `readme-design.md`.
- Does not change backend APIs, frontend runtime behavior, persistence behavior, deployment scripts, or dependencies.
