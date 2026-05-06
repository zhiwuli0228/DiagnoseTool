# Project Build and Deployment

This project uses lightweight PowerShell scripts to build, assemble, and run the Spring Boot backend and Vite frontend together.

## Prerequisites

- Java 21 on `PATH`
- Maven on `PATH`
- Node.js and npm on `PATH`

The scripts check these tools before doing build or startup work and fail with a prerequisite-specific message when one is missing.

## Commands

Run all commands from the repository root.

### Build

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

Useful options:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1 -SkipTests
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1 -SkipNpmInstall
```

Build outputs:

- Backend jar: `target/*.jar`
- Frontend assets: `frontend/dist/`

### Deploy

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1
```

Useful options:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -DeployDir E:\apps\thread-doctor
powershell -ExecutionPolicy Bypass -File .\scripts\deploy.ps1 -SkipBuild -BackendJar .\target\thread-doctor-0.1.0-SNAPSHOT.jar -FrontendDist .\frontend\dist
```

The default deploy directory is `deploy/`. Each deploy run refreshes the generated runtime directory. The generated layout is:

```text
deploy/
  app/
    thread-doctor.jar
  frontend/
    index.html
    assets/
  logs/
  runtime/
  app.env.example
  start.ps1
  status.ps1
  stop.ps1
```

## Runtime Configuration

Copy `deploy/app.env.example` to `deploy/app.env` and adjust values as needed.

```text
APP_PORT=8080
JAVA_OPTS=
SPRING_PROFILES_ACTIVE=
APP_ARGS=
```

Defaults:

- `APP_PORT`: `8080`
- `JAVA_OPTS`: empty
- `SPRING_PROFILES_ACTIVE`: empty
- `APP_ARGS`: empty

The startup script passes the frontend build directory to Spring Boot as an external static resource location, so the frontend is served by the backend process.

The backend jar produced by `scripts/build.ps1` is a Spring Boot executable jar and can run directly with `java -jar`. The deploy scripts wrap that jar with frontend assets, logs, PID tracking, and start/stop/status helpers.

## Start, Verify, Stop

Start the deployed application:

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\start.ps1
```

Verify process and HTTP availability:

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\status.ps1
```

Open:

```text
http://localhost:8080/
```

Stop the tracked process:

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\stop.ps1
```

Runtime files:

- PID file: `deploy/runtime/app.pid`
- stdout log: `deploy/logs/app.out.log`
- stderr log: `deploy/logs/app.err.log`

## LLM Runtime Configuration

The deployed frontend includes an `LLM configuration` panel. Operators can update `baseUrl`, `API key`, and `model` from the browser. Saved values are process-local runtime overrides and take effect for the next diagnosis request. Empty fields fall back to backend service configuration from `application*.yml` or environment variables. API keys are masked in read responses.
