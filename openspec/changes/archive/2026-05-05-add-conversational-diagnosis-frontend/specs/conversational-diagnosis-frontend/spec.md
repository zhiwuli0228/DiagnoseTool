## ADDED Requirements

### Requirement: Conversational diagnosis entry
The frontend SHALL provide a React-based conversational diagnosis entry that guides users through a complete diagnosis workflow.

#### Scenario: User starts a diagnosis conversation
- **WHEN** a user opens the diagnosis frontend with the conversational feature enabled
- **THEN** the system displays a conversation workspace for describing the incident and starting a guided diagnosis

#### Scenario: Feature switch disables the entry
- **WHEN** the conversational diagnosis feature switch is disabled
- **THEN** the system does not expose the conversational diagnosis workflow as an active entry

### Requirement: Incident session orchestration
The frontend SHALL create and track a backend incident session before uploading evidence or running diagnosis.

#### Scenario: Incident session is created
- **WHEN** the user submits an incident title, description, and severity from the conversation
- **THEN** the frontend calls the backend incident creation API and stores the returned session id and status

#### Scenario: Session creation fails
- **WHEN** the backend rejects or fails the incident creation request
- **THEN** the frontend shows a recoverable error message and keeps the user input available for retry

### Requirement: Evidence collection from conversation
The frontend SHALL allow users to submit supported evidence types from the conversation and persist them through backend APIs.

#### Scenario: User uploads log evidence
- **WHEN** the user submits a log snippet as evidence
- **THEN** the frontend uploads it as `LOG_SNIPPET` evidence for the active incident session

#### Scenario: User uploads jstack evidence
- **WHEN** the user submits jstack content as evidence
- **THEN** the frontend uploads it as `JSTACK` evidence for the active incident session

#### Scenario: User uploads metric evidence
- **WHEN** the user submits JVM, Redis, Kafka, or DB metric JSON from the conversation
- **THEN** the frontend sends the metric snapshot to the backend metrics API for the active incident session

### Requirement: Diagnosis report loop
The frontend SHALL trigger backend diagnosis and present the resulting report in the conversation flow.

#### Scenario: User runs diagnosis
- **WHEN** the user requests diagnosis for an active incident session
- **THEN** the frontend calls the backend diagnosis API, marks the conversation as diagnosing, and renders the returned report

#### Scenario: Diagnosis request fails
- **WHEN** the backend diagnosis request fails
- **THEN** the frontend marks the conversation as failed, displays the error, and allows the user to retry without losing submitted evidence state

### Requirement: Recovery and review closure
The frontend SHALL let users generate recovery recommendations and review simulated execution results from the diagnosis report.

#### Scenario: Recovery actions are generated
- **WHEN** the user asks for recovery recommendations after a report is available
- **THEN** the frontend calls the backend recovery action generation API and displays risk level, approval requirement, and verification guidance

#### Scenario: Recovery action is simulated
- **WHEN** the user executes a recovery action from the frontend
- **THEN** the frontend calls the backend simulated execution API and displays the returned simulated result

#### Scenario: Incident card is generated
- **WHEN** the user completes report and recovery review
- **THEN** the frontend can call the backend incident card API and display the generated retrospective card

### Requirement: Frontend concurrency guardrails
The frontend SHALL prevent duplicate long-running requests and SHALL apply configurable rate limits to user-triggered diagnosis actions.

#### Scenario: Duplicate diagnosis trigger is blocked
- **WHEN** a diagnosis task named for the active session is already running
- **THEN** the frontend blocks additional diagnosis submissions until the running task completes

#### Scenario: Request rate limit is applied
- **WHEN** the user repeatedly triggers evidence upload, diagnosis, or recovery generation within the configured limit window
- **THEN** the frontend suppresses excess requests and shows a clear waiting or disabled state

#### Scenario: Task names are observable
- **WHEN** the frontend starts a long-running diagnosis workflow task
- **THEN** the task is tracked with a stable English name containing the action type and session id

### Requirement: Frontend business tests
The frontend SHALL include unit tests for business behavior introduced by the conversational diagnosis workflow.

#### Scenario: Conversation flow is tested
- **WHEN** frontend tests run
- **THEN** they verify the incident creation, evidence submission, diagnosis, recovery recommendation, and completion states

#### Scenario: Concurrency behavior is tested
- **WHEN** frontend tests run
- **THEN** they verify duplicate request blocking, rate limiting, and feature switch behavior

### Requirement: Code and comments convention
The implementation SHALL use English for business code identifiers and SHOULD use Chinese comments only where they clarify non-obvious business intent or concurrency constraints.

#### Scenario: Business code naming is reviewed
- **WHEN** new frontend business modules are added
- **THEN** component names, function names, state names, and API client methods use English identifiers

#### Scenario: Comments explain key constraints
- **WHEN** code handles state transitions, task locks, rate limits, or feature switches
- **THEN** comments explain the intent in Chinese where the behavior is not self-evident
### Requirement: Internationalized frontend text
The frontend SHALL manage all user-visible interface text through internationalization resources and SHALL use Chinese as the default display language.

#### Scenario: Default language is Chinese
- **WHEN** a user opens the conversational diagnosis frontend without selecting a language
- **THEN** the system displays interface text, conversation prompts, status labels, button labels, form labels, error messages, and empty-state text in Chinese

#### Scenario: UI text is read from i18n resources
- **WHEN** frontend code renders user-visible text
- **THEN** the text is resolved from centralized i18n resources instead of being hard-coded directly in React components or workflow message builders

#### Scenario: Business enum values have display labels
- **WHEN** the frontend displays severity, evidence type, workflow stage, confidence, risk level, or execution mode values
- **THEN** the system displays localized labels instead of raw backend enum values where the value is visible to users

### Requirement: Key control tips
The frontend SHALL provide clear tips for key buttons and input fields in the conversational diagnosis workflow.

#### Scenario: Key action buttons provide tips
- **WHEN** the user focuses or hovers over buttons for incident creation, evidence upload, metric upload, diagnosis execution, recovery generation, simulated execution, or incident card generation
- **THEN** the system provides a localized tip explaining the action and whether it calls a backend API

#### Scenario: Key inputs provide tips
- **WHEN** the user focuses or hovers over incident fields, evidence content, evidence type, or metric JSON inputs
- **THEN** the system provides a localized tip explaining expected input and important format constraints

#### Scenario: Tips are testable
- **WHEN** frontend tests run
- **THEN** they verify that key buttons and inputs expose localized tips through accessible attributes or associated helper text
