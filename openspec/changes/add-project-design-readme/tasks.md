## 1. Repository Review

- [x] 1.1 Inspect backend packages, controllers, services, domain models, prompt templates, and deployment scripts.
- [x] 1.2 Inspect frontend workflow, API client, state reducer, i18n resources, and major UI panels.
- [x] 1.3 Review current OpenSpec main specs to avoid missing archived capabilities.

## 2. Design Document Content

- [ ] 2.1 Create root-level `readme-design.md`.
- [ ] 2.2 Summarize overall project purpose, runtime architecture, module boundaries, and data storage model.
- [ ] 2.3 Describe core capabilities: incident session, evidence management, jstack/metrics analysis, fault pattern detection, LLM diagnosis, unresolved diagnosis handoff, recovery actions, incident card, log intelligence, prompt management, frontend LLM configuration, security, and build/deploy scripts.
- [ ] 2.4 Document safety boundaries for API keys, sensitive-data masking, bounded inputs/outputs, in-memory caches, and document-only generated prompts.

## 3. Diagrams

- [ ] 3.1 Add a Mermaid `classDiagram` for key domain models and service relationships.
- [ ] 3.2 Add a Mermaid `sequenceDiagram` for the main frontend-to-backend diagnosis flow.
- [ ] 3.3 Add a Mermaid activity-style `flowchart` for the end-to-end user workflow.
- [ ] 3.4 Add any small supporting diagrams only if they clarify deployment or log-analysis flow without making the document noisy.

## 4. Verification

- [ ] 4.1 Check that Mermaid code blocks use valid standard syntax.
- [ ] 4.2 Check that `readme-design.md` does not include secrets, private local paths, or unsupported future capabilities.
- [ ] 4.3 Run a lightweight documentation verification by reading the final file and confirming required sections are present.
