## Context

The existing diagnosis flow is synchronous. The React page dispatches `diagnosisStarted`, calls `POST /api/incidents/{sessionId}/diagnose`, and waits for a final `DiagnosisReport`. The backend service marks the incident as `DIAGNOSING`, builds the diagnosis context, runs rule detection, calls an OpenAI-compatible LLM, validates JSON, stores the report in cache, and then returns the report.

This leaves the frontend with only a coarse "diagnosing" stage. Long LLM calls or slow evidence analysis look like a stalled page even though backend work may still be moving. The project currently uses application cache rather than database persistence, so progress must also remain transient.

## Goals / Non-Goals

**Goals:**

- Show a real backend-driven diagnosis progress bar during report generation.
- Keep progress session-scoped and cache-backed, consistent with the existing no-database MVP design.
- Let the frontend poll progress while `POST /diagnose` is in flight and stop cleanly on completion or failure.
- Keep progress display localized through existing frontend i18n resources.
- Cover progress repository/service, API contract, workflow state, polling, and UI with focused tests.

**Non-Goals:**

- Do not convert diagnosis execution to a background job queue.
- Do not add SSE, WebSocket, or streaming token delivery.
- Do not persist progress to MySQL, JPA, files, or any external store.
- Do not add progress tracking for recovery generation, simulated execution, or incident card generation.
- Do not change the diagnosis report response contract except for adding a separate progress query API.

## Decisions

### Decision 1: Cache-backed progress model

Create a `DiagnosisProgress` model keyed by `sessionId`, with fields such as `sessionId`, `status`, `percent`, `step`, `message`, `startedAt`, `updatedAt`, and optional `errorMessage`.

Rationale: progress is runtime state, not durable business data. A cache-backed model matches the existing incident, evidence, report, recovery, and result-document storage strategy.

Alternative considered: store progress in `IncidentSession`. That would avoid a new repository but would overload session status with step-level UI state and make future progress lifecycle harder to isolate.

### Decision 2: Backend writes progress at deterministic phase boundaries

`DiagnosisReportService.diagnose()` should update progress at key points:

```text
0   PENDING
10  STARTED
25  BUILDING_CONTEXT
45  DETECTING_PATTERNS
70  GENERATING_REPORT
90  VALIDATING_REPORT
100 COMPLETED
```

On errors, it should write `FAILED` with the last known percent and error message before rethrowing the existing exception.

Rationale: the backend owns the real phase transitions. Fixed phase percentages are simple, deterministic, and testable without pretending to know exact LLM completion percent.

Alternative considered: time-based progress interpolation. That improves perceived motion but becomes misleading when backend phases are uneven.

### Decision 3: Separate progress read API

Add `GET /api/incidents/{sessionId}/diagnosis-progress` returning the current `DiagnosisProgress`. If no progress exists, return a stable not-started progress object or a 404-compatible application error, whichever better matches existing controller error handling during implementation.

Rationale: a dedicated endpoint keeps the current `POST /diagnose` response unchanged and lets the frontend poll without re-fetching full incident details and evidence.

Alternative considered: reuse `GET /api/incidents/{sessionId}`. That would avoid a new endpoint but would mix progress with incident detail and force larger payloads during polling.

### Decision 4: Frontend polls while the diagnosis request is active

When `runDiagnosis()` starts, the frontend should:

1. Initialize progress in workflow state.
2. Start `POST /diagnose`.
3. Poll `GET /diagnosis-progress` at a configurable interval while the diagnosis task is running.
4. Render a progress bar with percent and localized step label.
5. Stop polling after the diagnosis request resolves, rejects, or the component unmounts.

Rationale: this works with the current synchronous backend and avoids adding server push infrastructure.

Alternative considered: wait for `POST /diagnose` only and simulate local progress. That is lower cost but does not satisfy the real backend progress requirement.

## Risks / Trade-offs

- [Risk] Polling can create extra backend requests during slow LLM calls. Mitigation: use a moderate configurable interval, for example 1000 ms, and only poll while the diagnosis task is active.
- [Risk] Progress may remain at the last phase if the backend crashes mid-request. Mitigation: cache TTL expires stale progress, and frontend can show the final request error when the POST fails.
- [Risk] Fixed percentages can look uneven because LLM generation may dominate total time. Mitigation: show both percent and current step label so users understand which phase is taking time.
- [Risk] Existing i18n resources currently contain corrupted Chinese text in places. Mitigation: include progress labels in i18n and consider cleaning affected i18n strings during implementation if tests depend on readable text.
- [Risk] Multiple diagnosis runs for the same session can overwrite progress. Mitigation: existing frontend task lock prevents duplicate runs; backend should reset progress at the start of each accepted diagnosis run.
