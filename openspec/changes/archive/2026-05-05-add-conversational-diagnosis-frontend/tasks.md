## 1. Frontend Foundation

- [x] 1.1 Review the current React entry and replace the minimal demo flow with a conversational diagnosis workspace.
- [x] 1.2 Add or confirm frontend test dependencies for React component behavior, hooks, and API orchestration.
- [x] 1.3 Create a centralized frontend configuration module for feature switches and request limit settings.
- [x] 1.4 Define English-named domain types for incident sessions, evidence drafts, diagnosis reports, recovery actions, incident cards, conversation messages, and workflow stages.

## 2. API Client and Workflow State

- [x] 2.1 Implement an English-named `diagnosisApi` client for incident creation, evidence upload, metric snapshot upload, diagnosis execution, recovery action generation, simulated execution, and incident card generation.
- [x] 2.2 Implement a reducer or equivalent state model for `idle`, `collectingEvidence`, `diagnosing`, `reviewingReport`, `recommendingRecovery`, `completed`, and `failed` stages.
- [x] 2.3 Add conversation message derivation so backend results and user actions appear as a guided diagnosis conversation.
- [x] 2.4 Preserve user drafts and submitted evidence state when API calls fail, so the user can retry without retyping.

## 3. Conversational Diagnosis UI

- [x] 3.1 Build the conversation layout with message history, active prompt, incident summary, and current workflow status.
- [x] 3.2 Add controls for incident title, description, severity, and starting a backend incident session.
- [x] 3.3 Add evidence input flows for log snippets, jstack content, and JVM/Redis/Kafka/DB metric JSON.
- [x] 3.4 Add diagnosis report rendering with summary, confidence, candidate root causes, missing information, and raw structured report details.
- [x] 3.5 Add recovery recommendation rendering with risk level, approval requirement, verification guidance, and simulated execution result.
- [x] 3.6 Add incident card generation and display after report and recovery review.

## 4. Concurrency, Rate Limiting, and Feature Switches

- [x] 4.1 Implement stable task naming for long-running frontend tasks such as `diagnosis-run:<sessionId>`, `evidence-upload:<sessionId>`, and `recovery-generate:<sessionId>`.
- [x] 4.2 Block duplicate submissions while a same-named task is running.
- [x] 4.3 Apply configurable client-side rate limits to evidence upload, diagnosis execution, and recovery generation actions.
- [x] 4.4 Hide or disable the conversational workflow when `VITE_ENABLE_CONVERSATIONAL_DIAGNOSIS` is disabled.
- [x] 4.5 Add concise Chinese comments around non-obvious task locking, rate limiting, and feature switch behavior.

## 5. Tests and Verification

- [x] 5.1 Add unit tests for the API client request paths and payload mapping.
- [x] 5.2 Add unit tests for workflow state transitions and conversation message derivation.
- [x] 5.3 Add component tests for creating an incident, uploading evidence, running diagnosis, generating recovery actions, simulating execution, and generating an incident card.
- [x] 5.4 Add tests for duplicate request blocking, rate limiting, retry behavior, and feature switch behavior.
- [x] 5.5 Run frontend tests and build commands, then fix failures.
- [x] 5.6 Run backend tests to confirm the frontend change did not regress existing diagnosis APIs.
## 6. Internationalization and Tips

- [x] 6.1 Add a lightweight frontend i18n module with `zh-CN` as the default locale.
- [x] 6.2 Move all user-visible UI text, conversation text, status text, error text, and empty-state text into i18n resources.
- [x] 6.3 Add i18n-backed labels for business enum values shown on the page.
- [x] 6.4 Add clear i18n-backed tips for key buttons and inputs, including incident creation, evidence upload, metric JSON input, diagnosis execution, recovery generation, simulated execution, and incident card generation.
- [x] 6.5 Update React component tests to use default Chinese UI text and verify key control tips.
- [x] 6.6 Run frontend tests and build commands, then fix failures.
