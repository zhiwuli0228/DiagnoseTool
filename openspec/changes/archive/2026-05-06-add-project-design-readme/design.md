## Context

Thread Doctor currently includes a Spring Boot backend, a Vite/React frontend, in-memory runtime stores, diagnosis workflows, log intelligence, prompt templates, LLM runtime configuration, security hardening, and build/deploy scripts. The project has feature-specific README and OpenSpec artifacts, but no single design document that summarizes how the whole product fits together.

The new documentation should be generated from the current repository shape and existing specs. It should help maintainers understand modules, dependencies, runtime flow, and core capabilities without reading every service class first.

## Goals / Non-Goals

**Goals:**
- Create `readme-design.md` at the repository root.
- Summarize the current project purpose, architecture, modules, runtime/deployment model, and core capabilities.
- Include Mermaid class diagrams, sequence diagrams, and activity diagrams that render in common Markdown viewers.
- Keep diagram scope useful and maintainable: show domain/service relationships and workflows, not every implementation class.
- Cover safety boundaries such as in-memory storage, environment-only API key handling, bounded generated prompts, and document-only Codex/OpenCode handoff.

**Non-Goals:**
- Do not change backend or frontend runtime behavior.
- Do not add new dependencies or diagram-generation tools.
- Do not document future roadmap items as implemented capabilities.
- Do not include secrets, environment-specific paths, or large generated bundles.

## Decisions

1. Use Mermaid diagrams embedded in Markdown.
   - Rationale: Mermaid is portable, reviewable, and supported by many Markdown viewers.
   - Alternative considered: generated PNG/SVG diagrams. Rejected because binary or generated artifacts are harder to diff and maintain.

2. Organize `readme-design.md` by reader workflow.
   - Rationale: Start with overall architecture, then core capabilities, then diagrams and runtime flows. This supports onboarding and maintenance.
   - Alternative considered: package-by-package reference. Rejected because it becomes verbose and duplicates code structure without explaining behavior.

3. Keep diagrams at capability/service level.
   - Rationale: The project contains many small domain and test classes; diagramming all classes would be noisy and brittle.
   - Alternative considered: exhaustive class diagram. Rejected because it would be hard to keep current and less useful for design review.

4. Use repository inspection as the source of truth.
   - Rationale: The document must summarize the current implementation, including recently archived changes.
   - Alternative considered: rely only on OpenSpec text. Rejected because specs describe intended behavior but may not capture exact package/module layout.

## Risks / Trade-offs

- Documentation drift -> Keep the document concise, reference major modules instead of every class, and include commands/workflows that are stable.
- Mermaid rendering differences -> Use standard `classDiagram`, `sequenceDiagram`, and `flowchart` syntax only.
- Overstating capabilities -> Describe only implemented behavior visible in current code/specs.
- Sensitive data leakage -> Do not include real API keys, local secrets, user evidence, or environment-specific private values.
