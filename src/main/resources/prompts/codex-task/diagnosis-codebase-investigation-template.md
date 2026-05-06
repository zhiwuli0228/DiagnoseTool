# Continue Diagnosis With Codebase Context

This prompt is generated only for handoff to Codex/OpenCode. Do not treat it as a confirmed root cause until code is inspected.

## Incident
{{incidentSummary}}

## Current Diagnosis
- Summary: {{diagnosisSummary}}
- Confidence: {{confidence}}
- Localization status: {{localizationStatus}}

## Why It Is Not Fully Localized
{{unresolvedReasons}}

## Evidence Already Available
{{evidenceSummary}}

## Deterministic Pattern Results
{{detectionSummary}}

## Missing Or Requested Evidence
{{missingInformation}}

## Codebase Investigation Goal
Use the current repository to verify whether the evidence can localize a concrete root cause. Focus on the smallest code/config path that explains the symptoms.

## Required Output
- Confirmed or rejected root cause with file and method references.
- Evidence gaps if the repository still cannot localize the issue.
- Minimal fix proposal only if the root cause is verified.
- Focused tests that reproduce or guard the verified issue.

## Constraints
- Do not assume the generated diagnosis is correct without checking code.
- Do not make unrelated changes.
- Mask or avoid repeating secrets.
- Keep the investigation bounded to the symptoms and evidence above.
