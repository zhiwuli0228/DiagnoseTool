# Thread Doctor

Lightweight build, deployment, and startup flow for the Spring Boot backend and Vite frontend.

## Build

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

Outputs:

- Backend jar: `target\thread-doctor-0.1.0-SNAPSHOT.jar`
- Frontend assets: `frontend\dist\`

The backend jar is a Spring Boot executable jar and can run directly:

```powershell
java -jar .\target\thread-doctor-0.1.0-SNAPSHOT.jar
```

## Deploy Existing Build

After building, deploy without rebuilding:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild
```

Or pass explicit artifact paths:

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

Default URL: `http://localhost:8080/`

## LLM Configuration

After startup, use the frontend `LLM configuration` panel to update `baseUrl` and `model`.

- Saved values take effect for the next diagnosis request without restarting the backend.
- Fields not configured in the frontend continue to use backend defaults.
- API keys are not configurable from the frontend or YAML files. Set `LLM_API_KEY` in the runtime environment.

## Unresolved Diagnosis Handoff

If a diagnosis cannot localize the root cause, the report shows unresolved reasons, requested follow-up evidence, and a copy-only Codex/OpenCode prompt for codebase investigation.

Submit the requested evidence and run diagnosis again, or copy the prompt into Codex/OpenCode. Thread Doctor does not execute the prompt automatically.
