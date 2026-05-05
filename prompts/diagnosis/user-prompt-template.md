# Incident Diagnosis Request

Please diagnose the following production incident based on the Thread Doctor Evidence Pack.

## User Goal

{{userGoal}}

## Incident Context

{{incidentContext}}

## Evidence Pack

{{evidencePackJson}}

## Output Format

Please output the diagnosis report in the following structure:

1. One-sentence conclusion
2. Severity assessment
3. Root cause candidates Top 3
4. Supporting evidence
5. Incident timeline
6. Evidence chain
7. Excluded causes
8. Mitigation actions
9. Long-term fixes
10. Missing information
11. Codex investigation task
12. OpenSpec change draft
13. Limitations

## Additional Requirements

- Use Chinese for the final report.
- Keep technical class names, method names, exception names, traceId, and command names in their original form.
- Do not invent missing data.
- If the diagnosis requires source code confirmation, clearly mark it as `待 Codex 走读确认`.
- Every conclusion must reference evidence from the Evidence Pack.
- If evidence is insufficient, explicitly state `证据不足`.
- Separate observed facts, reasonable inferences, and missing information.