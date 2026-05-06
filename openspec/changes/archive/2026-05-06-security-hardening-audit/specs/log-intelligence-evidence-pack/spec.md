## MODIFIED Requirements

### Requirement: Log source ingestion
The system SHALL ingest log sources from ZIP uploads, browser-selected local directory uploads, and configured local directory scans while enforcing production safety limits, content-type constraints, path safety, and decompression boundaries.

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
- **WHEN** a ZIP archive exceeds configured file count, size, compression ratio, nesting depth, per-entry size, or supported content-type limits
- **THEN** the system rejects the source and records a structured limit error without parsing additional entries

#### Scenario: Scan an allowed local directory
- **WHEN** directory scan is enabled and the requested path is under a configured allowlist root
- **THEN** the system scans supported log files and associates summaries with the session

#### Scenario: Upload browser-selected local directory files
- **WHEN** a client uploads files selected from a local log directory
- **THEN** the system parses the files using their relative paths, applies the same source safety limits, and associates summaries with the session

#### Scenario: Reject disallowed local directory
- **WHEN** directory scan is disabled or the requested path is outside configured allowlist roots
- **THEN** the system rejects the request with a structured configuration or path safety error

### Requirement: Sensitive data masking and output limits
The system SHALL mask sensitive data and enforce bounded raw text, stack trace, sample log, search result, generated artifact, and response payload sizes.

#### Scenario: Mask sensitive values
- **WHEN** parsed logs or generated outputs contain IPs, emails, phone-like values, passwords, tokens, secrets, keys, API keys, authorization headers, cookies, or similar credential fields
- **THEN** the system replaces those values with stable masked placeholders before returning or storing derived output

#### Scenario: Bound raw excerpts
- **WHEN** a log event or evidence item contains long raw text or stack traces
- **THEN** the system truncates the field according to configured output caps and preserves a marker that truncation occurred

#### Scenario: Prevent sensitive generated artifacts
- **WHEN** the system generates Evidence Pack Markdown, Codex task Markdown, OpenSpec draft Markdown, diagnosis snippets, or frontend-visible log search results
- **THEN** the generated output MUST preserve masking and MUST NOT include plaintext secrets detected from uploaded logs or frontend inputs

### Requirement: Log event search
The system SHALL provide bounded search over parsed log events using time range, levels, multiple keywords, ignore-case option, duplicate compression option, trace ID, thread name, logger name, exception type, source file, limit, and stack trace inclusion filters while validating every search filter.

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

#### Scenario: Reject unsafe search filters
- **WHEN** a client submits excessive keyword length, excessive line fragments, invalid time range, invalid levels, invalid limit, or oversized filter values
- **THEN** the system rejects the search with a structured validation error before scanning cached events
