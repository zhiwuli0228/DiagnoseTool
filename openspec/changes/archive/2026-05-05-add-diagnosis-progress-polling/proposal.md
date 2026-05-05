## Why

The current diagnosis workflow only shows a coarse "diagnosing" state while the backend performs context building, rule detection, LLM generation, validation, and report persistence synchronously. Users need visible progress so they can understand whether diagnosis is still moving and which backend phase is currently running.

## What Changes

- Add cached backend diagnosis progress for each incident session, including status, percent, current step, step label, timestamps, and an optional error message.
- Update the diagnosis service to write progress at key phases: initialization, context building, rule detection, LLM generation, report validation, report persistence, completion, and failure.
- Add a read API for the frontend to query the current diagnosis progress by session id.
- Update the React conversational diagnosis page to poll the progress API while diagnosis is running and render an i18n-backed progress bar with the current step.
- Stop polling when diagnosis completes, fails, or the diagnosis request returns.
- Keep progress data in application cache only. Do not add database persistence, background job execution, SSE, or WebSocket delivery in this change.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `diagnosis-report`: add session-scoped cached diagnosis progress tracking and a query contract for current progress.
- `conversational-diagnosis-frontend`: add a progress bar that polls backend progress while diagnosis is running.

## Impact

- Backend: diagnosis progress domain model, in-memory progress repository/service, diagnosis service progress updates, diagnosis controller progress endpoint, API contract tests.
- Frontend: API client method, workflow state for diagnosis progress, progress polling lifecycle, progress bar UI, i18n labels/tips, React tests.
- Configuration: optional frontend polling interval and backend progress cache TTL/capacity may reuse existing cache defaults or add scoped properties.
- Storage: no database tables or external infrastructure; progress remains transient cache data.
