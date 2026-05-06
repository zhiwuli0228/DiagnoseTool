## Context

`docs/code-style.md` defines Java source style expectations around Javadoc and copyright headers. The current codebase has many Java classes across backend modules, so applying this requirement is cross-cutting and should be done with a repeatable inspection and verification step rather than ad hoc edits.

The visible examples in `docs/code-style.md` require public classes and public methods to have Javadoc with author/since metadata where applicable, meaningful parameter/return/throws documentation, valid Javadoc tags, and Java files to include a standard copyright header.

## Goals / Non-Goals

**Goals:**
- Add or normalize copyright headers for Java source files.
- Add or normalize Javadoc for public classes and public methods.
- Ensure Javadoc tags are valid and useful: no missing descriptions for public parameters, return values, or thrown exceptions.
- Add a lightweight verification path so regressions are easy to catch.

**Non-Goals:**
- Do not change runtime behavior, API contracts, persistence, deployment scripts, or frontend behavior.
- Do not rewrite private/internal comments unless they block verification.
- Do not perform broad formatting churn unrelated to copyright/Javadoc requirements.

## Decisions

1. Treat `docs/code-style.md` as the source of truth and implement the smallest practical Java style baseline.
   - Rationale: The document is the user-provided requirement source.
   - Alternative considered: invent a broader style guide. Rejected because it would expand scope beyond the request.

2. Prefer automated verification if the project can support it with low risk.
   - Rationale: Javadoc/header rules drift quickly if they are only manually reviewed.
   - Alternative considered: one-time manual cleanup only. Rejected because it does not prevent regression.

3. Keep content comments succinct and business-relevant.
   - Rationale: Javadocs should explain public API intent without adding noisy restatements of names.
   - Alternative considered: generate boilerplate descriptions for every method. Rejected because low-signal comments reduce maintainability.

## Risks / Trade-offs

- [Large edit surface] -> Apply changes in focused batches and avoid unrelated refactors.
- [Noisy generated comments] -> Prefer concise meaningful summaries and only document public API surface.
- [Build/tooling incompatibility] -> If style tooling is added, keep it Maven-compatible and verify with existing test/build commands.
- [Ambiguous copyright year range] -> Use the current year from the environment unless existing project convention indicates otherwise.
