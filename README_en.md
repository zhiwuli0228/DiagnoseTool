# Thread Doctor

Thread Doctor is a lightweight Java production diagnosis tool with a Spring Boot backend, a Vite frontend, and a local Sidecar log-analysis service.

## Build

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

Outputs:

- Backend jar: `target\thread-doctor-0.1.0-SNAPSHOT.jar`
- Frontend assets: `frontend\dist\`

The jar can run directly:

```powershell
java -jar .\target\thread-doctor-0.1.0-SNAPSHOT.jar
```

## Deploy

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild
```

Or pass existing artifacts:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild -BackendJar .\target\thread-doctor-0.1.0-SNAPSHOT.jar -FrontendDist .\frontend\dist
```

Default deploy directory: `deploy\`

## Start And Stop

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\start.ps1
powershell -ExecutionPolicy Bypass -File .\deploy\status.ps1
powershell -ExecutionPolicy Bypass -File .\deploy\stop.ps1
```

Default URLs:

- Frontend and backend: `http://localhost:8080/`
- Sidecar health: `http://127.0.0.1:18765/api/sidecar/health`

To change ports, copy `deploy\app.env.example` to `deploy\app.env` and edit:

```properties
APP_PORT=8080
SIDECAR_PORT=18765
```

## Large Log Workflow

Large ZIP files and directories use Sidecar mode by default:

1. Start `deploy\start.ps1`; it starts both the backend and the Sidecar.
2. Enter the local ZIP path or directory path in the frontend log section.
3. Run Sidecar local analysis.
4. Decompression, parsing, masking, clustering, timeline generation, and evidence extraction run locally.
5. The frontend displays the result; after confirmation, only sanitized structured results or selected key excerpts are submitted to the backend.

Raw logs are not uploaded to the backend by default. File upload buttons are only for small-file compatibility mode and are not recommended for production-sized logs.

## LLM Configuration

Use the frontend `LLM configuration` panel to update `baseUrl` and `model`. New values take effect on the next diagnosis request.

API keys are read only from the `LLM_API_KEY` environment variable and are not stored in frontend or YAML configuration.

## Unresolved Diagnosis Handoff

If a diagnosis cannot localize the root cause, the system returns unresolved reasons, requested follow-up evidence, and a copy-only Codex/OpenCode prompt for codebase investigation. Thread Doctor does not execute that prompt automatically.
