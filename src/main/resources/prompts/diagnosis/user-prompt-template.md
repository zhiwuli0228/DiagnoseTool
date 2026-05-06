Diagnose the current incident.

## Goal
{{userGoal}}

## Incident Context
{{incidentContext}}

## Evidence Pack
{{evidencePackJson}}

## Instructions
- Identify the most likely root cause supported by evidence.
- Set localizationStatus to LOCALIZED only when the provided evidence can identify a concrete root cause.
- Set localizationStatus to NEEDS_MORE_EVIDENCE when the user can provide specific missing logs, jstack, metrics, trace IDs, or timing details to continue diagnosis.
- Set localizationStatus to UNRESOLVED when the evidence cannot localize the issue and codebase investigation is needed.
- For unresolved or incomplete localization, include unresolvedReasons and followUpEvidenceRequests.
- List missing information when evidence is insufficient.
- Keep evidence IDs in the response.
- Return JSON only.
