## ADDED Requirements

### Requirement: Log analysis sessions
The system SHALL provide cache-backed log analysis sessions for transient log ingestion and derived analysis output without introducing database persistence.

#### Scenario: Create a log analysis session
- **WHEN** a client requests a new log analysis session
- **THEN** the system creates a session ID and returns session metadata with an initial status

#### Scenario: Retrieve a log analysis session
- **WHEN** a client requests an existing log analysis session by session ID
- **THEN** the system returns session metadata, source summaries, processing status, and structured errors if any exist

### Requirement: Log source ingestion
The system SHALL ingest log sources from ZIP uploads, browser-selected local directory uploads, and configured local directory scans while enforcing production safety limits.

#### Scenario: Upload a valid ZIP source
- **WHEN** a client uploads a ZIP archive within configured limits
- **THEN** the system extracts supported log files, records file summaries, and associates them with the session

#### Scenario: Parse nested ZIP log archives
- **WHEN** a valid uploaded ZIP contains nested ZIP log archives such as `collect_xxx.log.zip`
- **THEN** the system recursively parses supported log files inside the nested archive and applies the same file count, size, compression ratio, and ZIP slip safeguards

#### Scenario: Reject ZIP path traversal
- **WHEN** a ZIP entry attempts to write outside the extraction root
- **THEN** the system rejects the source and returns a structured ZIP slip error

#### Scenario: Reject unsafe archive limits
- **WHEN** a ZIP archive exceeds configured file count, size, or decompression ratio limits
- **THEN** the system rejects the source and records a structured limit error

#### Scenario: Scan an allowed local directory
- **WHEN** directory scan is enabled and the requested path is under a configured allowlist root
- **THEN** the system scans supported log files and associates summaries with the session

#### Scenario: Upload browser-selected local directory files
- **WHEN** a client uploads files selected from a local log directory
- **THEN** the system parses the files using their relative paths, applies the same source safety limits, and associates summaries with the session

#### Scenario: Reject disallowed local directory
- **WHEN** directory scan is disabled or the requested path is outside configured allowlist roots
- **THEN** the system rejects the request with a structured configuration or path safety error

### Requirement: Log event parsing
The system SHALL normalize ingested log content into `LogEvent` records containing timestamp, level, threadName, loggerName, traceId, message, exceptionType, stackTrace, rawText, sourceFile, lineNumber, and tags where available.

#### Scenario: Parse a structured Java log line
- **WHEN** a supported Java application log line contains timestamp, level, thread, logger, and message fields
- **THEN** the system creates a `LogEvent` with those fields populated

#### Scenario: Extract supported trace ID patterns
- **WHEN** a log line contains `traceId=xxx`, `trace_id=xxx`, `traceId: xxx`, `[traceId:xxx]`, or `X-B3-TraceId=xxx`
- **THEN** the system stores the extracted trace ID on the `LogEvent`

#### Scenario: Fold Java stack traces
- **WHEN** a log event is followed by Java stack trace lines including `at`, `Caused by`, `Suppressed`, or common frames omitted lines
- **THEN** the system attaches the stack trace to the event and extracts the exception type when possible

#### Scenario: Preserve unparseable content
- **WHEN** a line cannot be parsed into a supported structured event
- **THEN** the system records an `UNPARSED` event with bounded raw text and source location

### Requirement: Sensitive data masking and output limits
The system SHALL mask sensitive data and enforce bounded raw text, stack trace, sample log, and response payload sizes.

#### Scenario: Mask sensitive values
- **WHEN** parsed logs or generated outputs contain IPs, emails, phone-like values, passwords, tokens, secrets, keys, or similar credential fields
- **THEN** the system replaces those values with stable masked placeholders before returning or storing derived output

#### Scenario: Bound raw excerpts
- **WHEN** a log event or evidence item contains long raw text or stack traces
- **THEN** the system truncates the field according to configured output caps and preserves a marker that truncation occurred

### Requirement: Log event search
The system SHALL provide bounded search over parsed log events using time range, levels, multiple keywords, ignore-case option, duplicate compression option, trace ID, thread name, logger name, exception type, source file, limit, and stack trace inclusion filters.

#### Scenario: Filter log events
- **WHEN** a client searches with one or more supported filters
- **THEN** the system returns only matching events up to the requested or configured maximum limit

#### Scenario: Search textual fields
- **WHEN** a client searches by keyword
- **THEN** the system matches against message, stackTrace, rawText, loggerName, exceptionType, traceId, threadName, sourceFile, and tags fields

#### Scenario: Search multi-fragment keyword input
- **WHEN** a client searches with multiple keyword fragments separated by line breaks
- **THEN** the system treats each non-empty line as a searchable keyword fragment
- **AND** spaces, commas, semicolons, Chinese commas, and Chinese semicolons inside a fragment are preserved as searchable text

#### Scenario: Search with case sensitivity preference
- **WHEN** a client searches with ignore-case enabled or omitted
- **THEN** the system matches textual filters case-insensitively by default
- **AND WHEN** ignore-case is disabled
- **THEN** the system matches textual filters case-sensitively

#### Scenario: Compress duplicate log rows
- **WHEN** a client searches with duplicate compression enabled
- **THEN** the system returns one representative row for identical log content
- **AND** the representative row includes the number of compressed duplicate occurrences

#### Scenario: Exclude stack traces from search results
- **WHEN** a client sets `includeStackTrace` to false
- **THEN** the system omits stack trace bodies from returned event rows while keeping match metadata

### Requirement: Exception clustering
The system SHALL cluster similar log events by fingerprints derived from exception type, normalized message, and top stack frames.

#### Scenario: Normalize volatile tokens
- **WHEN** messages differ only by UUIDs, IPs, trace IDs, cache keys, or similar volatile tokens
- **THEN** the system assigns them the same normalized fingerprint when exception type and top frames also match

#### Scenario: Return sorted clusters
- **WHEN** a client requests clusters for a session
- **THEN** the system returns `LogCluster` records sorted by severity, count, and firstSeen with bounded sample logs

### Requirement: Incident timeline
The system SHALL build an incident timeline from important warnings, errors, repeated failures, high-risk exceptions, high-risk keywords, and trace-linked events.

#### Scenario: Build a chronological timeline
- **WHEN** parsed events include ERROR, WARN, repeated errors, high-risk exceptions, or keywords such as timeout, failed, rejected, oom, deadlock, pool exhausted, and connection refused
- **THEN** the system returns timeline events ordered by time with source file, thread, trace ID, related cluster, and evidence event references where available

#### Scenario: Link events by trace ID
- **WHEN** multiple events share the same trace ID
- **THEN** the system links related events in the timeline so the incident path can be followed

### Requirement: Evidence Pack generation
The system SHALL generate Evidence Pack output as JSON and Markdown from parsed events, clusters, timeline, evidence items, and suspected code areas.

#### Scenario: Extract evidence items
- **WHEN** a session contains errors, key exceptions, high-risk exception clusters, trace-linked chains, stack trace snippets, or important infrastructure keywords
- **THEN** the system creates bounded `EvidenceItem` records with confidence, source references, raw excerpt, related classes, and related methods

#### Scenario: Extract suspected code areas
- **WHEN** stack traces or logger names include candidate application classes or methods
- **THEN** the system records suspected code areas and prefers business packages over excluded framework packages such as `java`, `javax`, `jakarta`, `sun`, `jdk`, `org.springframework`, `redis.clients`, `org.apache`, `com.zaxxer`, and `org.slf4j`

#### Scenario: Generate JSON and Markdown evidence pack
- **WHEN** a client requests an Evidence Pack for a processed session
- **THEN** the system returns source summary, log file summary, incident summary, key clusters, timeline, evidence items, suspected code areas, recommended Codex questions, recommended checks, and limitations in JSON or Markdown form

#### Scenario: Avoid codebase scanning
- **WHEN** Evidence Pack generation runs
- **THEN** the system uses only ingested log evidence and does not scan or upload the application codebase

### Requirement: Frontend log snippet integration
The system SHALL provide React frontend controls that use log analysis sessions as first-class input sources for log snippet evidence.

#### Scenario: Upload ZIP logs into log snippet evidence
- **WHEN** a user selects a ZIP log bundle from the log snippet evidence area and starts frontend log parsing
- **THEN** the frontend creates a log analysis session, uploads the ZIP bundle, renders bounded session status and derived summaries, and waits for user keyword extraction before writing any parsed log content into the `LOG_SNIPPET` evidence draft

#### Scenario: Submit directory scan into log snippet evidence
- **WHEN** a user enters a directory path from the log snippet evidence area and starts frontend directory parsing
- **THEN** the frontend creates a log analysis session, submits the directory scan request, renders either parsed results or the backend structured safety error, and waits for user keyword extraction before writing any parsed log content into the `LOG_SNIPPET` evidence draft

#### Scenario: Extract relevant logs by user keywords
- **WHEN** a processed log analysis session exists and the user enters one or more keywords, optional selected levels, optional time range, extraction limit, stack trace preference, ignore-case preference, and duplicate compression preference
- **THEN** the frontend calls the bounded log search API and renders only the matching event subset for review

#### Scenario: Choose one frontend log source
- **WHEN** the log snippet evidence area is rendered
- **THEN** the frontend lets the user choose either a ZIP upload or a local directory upload, but not both for the same parse action

#### Scenario: Prevent large automatic LLM input
- **WHEN** ZIP upload or directory scan completes for a large log source
- **THEN** the frontend SHALL NOT automatically submit Evidence Pack Markdown, full parsed logs, clusters, or timeline content to diagnosis evidence

#### Scenario: Submit parsed logs through diagnosis evidence flow
- **WHEN** keyword extraction has populated the log snippet evidence draft and the user submits evidence
- **THEN** the frontend submits the parsed log findings using the existing `LOG_SNIPPET` evidence API for the active diagnosis session

#### Scenario: Provide tips for key controls
- **WHEN** the log snippet evidence controls are rendered
- **THEN** ZIP upload, directory path input, parse actions, and the log snippet input expose clear user tips using localized text

#### Scenario: Keep large parse summaries readable
- **WHEN** parsed log sessions contain many files, opaque cluster IDs, or long timeline summaries
- **THEN** the frontend shows a limited file summary with displayed/total count, renders cluster rows using meaningful exception/sample/class labels before internal IDs, and constrains timeline content so severity labels and actions remain usable
