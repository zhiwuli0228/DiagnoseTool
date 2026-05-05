## 1. Backend Progress Model

- [x] 1.1 Define `DiagnosisProgressStatus` and `DiagnosisProgressStep` enums with English names for not started, running phases, completed, and failed states.
- [x] 1.2 Add a `DiagnosisProgress` model containing session id, status, percent, step, message, started time, updated time, and optional error message.
- [x] 1.3 Add a cache-backed `DiagnosisProgressRepository` or service using the existing in-memory cache style and no database dependencies.
- [x] 1.4 Add unit tests for progress save, lookup, completion, failure, and cache miss behavior.

## 2. Backend Diagnosis Integration

- [x] 2.1 Update `DiagnosisReportService.diagnose()` to reset progress at the start of each accepted diagnosis run.
- [x] 2.2 Write progress updates for context building, rule detection, LLM generation, report validation, report persistence, completion, and failure.
- [x] 2.3 Ensure progress percent never decreases during a single diagnosis run.
- [x] 2.4 Preserve existing diagnosis error behavior while recording failed progress before propagating errors.
- [x] 2.5 Add service tests that verify progress transitions for successful and failed diagnosis runs.

## 3. Backend API Contract

- [x] 3.1 Add `GET /api/incidents/{sessionId}/diagnosis-progress` to return current progress for the session.
- [x] 3.2 Define the missing-progress response behavior as either deterministic not-started progress or existing not-found error handling, and keep it stable for the frontend.
- [x] 3.3 Add API contract tests for running progress, completed progress, and missing progress.

## 4. Frontend API and Workflow State

- [x] 4.1 Add a `getDiagnosisProgress(sessionId)` method to the frontend diagnosis API client.
- [x] 4.2 Extend frontend domain/workflow state with diagnosis progress status, percent, step, message, and optional polling warning.
- [x] 4.3 Add reducer actions for progress reset, progress received, progress polling warning, and progress failure.
- [x] 4.4 Add unit tests for progress-related workflow transitions and API client endpoint mapping.

## 5. Frontend Polling and UI

- [x] 5.1 Start progress polling when `runDiagnosis()` begins for the active incident session.
- [x] 5.2 Stop polling when diagnosis succeeds, diagnosis fails, the component unmounts, or the running diagnosis task ends.
- [x] 5.3 Keep polling failures recoverable without failing the active diagnosis request.
- [x] 5.4 Render an accessible progress bar with percent, localized step label, and current progress message while diagnosis is running.
- [x] 5.5 Add i18n resources for progress statuses, steps, labels, warnings, and tips with Chinese as the default display language.
- [x] 5.6 Add React component tests for progress rendering, polling updates, cleanup, and recoverable polling failure.
- [x] 5.7 Enhance the progress display to show the diagnosis step chain and the current active work item while retaining the progress percent.

## 6. Verification

- [x] 6.1 Run backend tests with `mvn test` and fix failures.
- [x] 6.2 Run frontend tests with `npm.cmd test` and fix failures.
- [x] 6.3 Run frontend build with `npm.cmd run build` and fix failures.
- [x] 6.4 Run `openspec.cmd status --change "add-diagnosis-progress-polling"` to confirm the change is apply-ready.
