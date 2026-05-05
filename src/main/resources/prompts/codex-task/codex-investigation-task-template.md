# Investigate Production Incident From Evidence Pack

## Incident Summary
{{incidentSummary}}

## Key Evidence
{{keyEvidence}}

## Timeline
{{timeline}}

## Suspected Code Areas
{{suspectedCodeAreas}}

## Questions to Answer
- What root cause is best supported by the log evidence?
- Which code path owns the suspected business stack frame?
- What focused test can reproduce or guard the issue?

## Required Codebase Investigation
- Inspect the suspected classes and methods.
- Verify assumptions against current code before changing behavior.
- Trace configuration that controls timeouts, pools, retries, and downstream calls.

## Required Changes if Root Cause Confirmed
- Make the smallest code or configuration change that addresses the verified root cause.
- Keep unrelated behavior unchanged.

## Tests to Add
- Add JUnit 5 and Mockito coverage for the verified root cause.
- Include regression coverage for the failure path.

## Engineering Constraints
- Use JDK 21 and Maven.
- Use JUnit 5 with Mockito.
- Do not use PowerMock.
- Do not guess without evidence.
- Keep code identifiers in English.
- Use Chinese comments only where necessary.
- Do not make unrelated changes.
- Run `mvn test`.

## Do Not
- Do not scan unrelated code.
- Do not change public behavior unless the root cause requires it.
- Do not treat this generated task as proof without codebase verification.
