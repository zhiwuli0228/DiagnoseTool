## Why

The project needs a consistent Java source style baseline so public APIs and extension points remain readable and maintainable as the codebase grows. The source requirements in `docs/code-style.md` call out missing or inconsistent Javadoc and Java file copyright headers.

## What Changes

- Add a Java code style capability covering public class and public method Javadoc.
- Require Java source files to include a standard copyright header.
- Add implementation tasks to inspect existing Java files, add missing compliant comments/headers, and verify the result.
- Do not change runtime behavior, APIs, persistence, deployment scripts, or frontend behavior.

## Capabilities

### New Capabilities
- `java-code-style`: Defines Java source style requirements for copyright headers and Javadoc on public classes and public methods.

### Modified Capabilities

None.

## Impact

- Affects Java source files under `src/main/java` and possibly test/support Java files if they are in scope during implementation.
- May add or configure style verification tooling if the existing build does not already enforce these requirements.
- No expected API, dependency, data model, or runtime behavior change.
