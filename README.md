# Thread Doctor

Spring Boot backend with a Vite frontend for Java incident diagnosis.

## Build

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

Outputs:
- Backend jar: `target\thread-doctor-0.1.0-SNAPSHOT.jar`
- Frontend assets: `frontend\dist\`

The backend jar is a Spring Boot executable jar:

```powershell
java -jar .\target\thread-doctor-0.1.0-SNAPSHOT.jar
```

## Deploy

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild
```

Deploy specific build outputs:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild -BackendJar .\target\thread-doctor-0.1.0-SNAPSHOT.jar -FrontendDist .\frontend\dist
```

Deployment output: `deploy\`

## Run

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\start.ps1
powershell -ExecutionPolicy Bypass -File .\deploy\status.ps1
powershell -ExecutionPolicy Bypass -File .\deploy\stop.ps1
```

Default URL: `http://localhost:8080/`

## LLM Configuration

The frontend `LLM configuration` panel can update `baseUrl` and `model`. API keys are not configurable from the frontend or YAML files; set `LLM_API_KEY` in the runtime environment.

## Unresolved Diagnosis Handoff

If a diagnosis cannot localize the root cause, the report shows:

- unresolved reasons
- requested follow-up evidence
- a copy-only Codex/OpenCode prompt for codebase investigation

Submit the requested evidence and run diagnosis again, or copy the prompt into Codex/OpenCode. Thread Doctor does not execute the prompt automatically.

## Security

- Set `LLM_API_KEY` in production; API keys are not stored in `application*.yml` or frontend runtime configuration.
- Request size, upload, search, evidence, metrics, and generated artifact limits are documented in [docs/security-hardening.md](docs/security-hardening.md).
- Keep sensitive data masking enabled in production.
