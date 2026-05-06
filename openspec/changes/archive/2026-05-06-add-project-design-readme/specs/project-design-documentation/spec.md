## ADDED Requirements

### Requirement: Project design README
The system SHALL include a root-level `readme-design.md` that summarizes the current project design and implemented capabilities.

#### Scenario: Design document exists
- **WHEN** the documentation change is implemented
- **THEN** the repository MUST contain `readme-design.md` at the project root

#### Scenario: Current functionality is summarized
- **WHEN** a maintainer reads `readme-design.md`
- **THEN** the document MUST summarize the project's overall purpose, frontend service, backend service, diagnosis workflow, evidence handling, log intelligence, prompt/LLM configuration, recovery/review flow, security boundaries, and build/deployment flow

### Requirement: Architecture diagrams
The design document SHALL include Markdown-renderable Mermaid diagrams for the current system.

#### Scenario: Class diagram is available
- **WHEN** `readme-design.md` is rendered by a Mermaid-capable Markdown viewer
- **THEN** it MUST include at least one `classDiagram` describing key domain models and service relationships

#### Scenario: Sequence diagram is available
- **WHEN** `readme-design.md` is rendered by a Mermaid-capable Markdown viewer
- **THEN** it MUST include at least one `sequenceDiagram` describing the incident diagnosis flow from frontend action to backend report response

#### Scenario: Activity diagram is available
- **WHEN** `readme-design.md` is rendered by a Mermaid-capable Markdown viewer
- **THEN** it MUST include at least one activity-style Mermaid `flowchart` describing the end-to-end user workflow

### Requirement: Maintainable documentation scope
The design document SHALL be concise, current, and safe to share inside the project.

#### Scenario: Documentation avoids implementation noise
- **WHEN** `readme-design.md` describes classes or modules
- **THEN** it MUST focus on core capabilities and major service/domain relationships instead of listing every source file

#### Scenario: Documentation avoids sensitive data
- **WHEN** `readme-design.md` includes configuration or security notes
- **THEN** it MUST NOT include real API keys, secrets, personal data, or local private paths

#### Scenario: Documentation is implementation-neutral
- **WHEN** `readme-design.md` describes generated Codex/OpenCode prompts
- **THEN** it MUST state that generated prompts are document-only handoff artifacts and are not executed automatically
