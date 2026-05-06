## Context

The repository contains a Spring Boot backend built with Maven at the project root and a Vite React frontend under `frontend/`. Each module already has its own build command, but there is no repository-level operational workflow that packages both sides, assembles a deployable layout, and starts the application with minimal manual steps.

This change introduces lightweight scripts and documentation for development machines and small-server deployments. The scripts should rely on the existing Java 21, Maven, Node.js, and npm toolchain instead of introducing a new runtime platform.

## Goals / Non-Goals

**Goals:**
- Provide one-command packaging for backend and frontend artifacts.
- Produce a predictable deployable directory that can be copied or run in place.
- Provide start and stop scripts for the backend service and frontend static service path.
- Keep configuration explicit through environment variables and example files.
- Support Windows first because the current workspace is Windows, while keeping script structure portable enough for later shell equivalents.

**Non-Goals:**
- Do not introduce Kubernetes, Docker Compose, or cloud deployment automation as part of this change.
- Do not replace Maven, npm, or Vite build behavior.
- Do not implement a full CI/CD pipeline.
- Do not change business APIs or diagnosis workflow behavior.

## Decisions

1. Use repository-root scripts as the operator entry point.

   Rationale: root-level commands reduce context switching between backend and frontend folders and make the happy path discoverable. The alternative was documenting separate Maven and npm commands only, but that would not satisfy the requirement for fast deployment and startup.

2. Assemble a lightweight `deploy/` directory.

   Rationale: a copied runtime layout with backend jar, frontend static assets, config examples, logs directory, and helper scripts is easy to inspect and transfer. The alternative was running directly from `target/` and `frontend/dist/`, but that couples runtime operations to build output internals.

3. Serve frontend assets through the backend static resources or a lightweight script-managed static server, depending on implementation fit.

   Rationale: the requirement covers both frontend and backend services while prioritizing lightweight startup. If the backend can package and serve frontend assets cleanly, one service is preferred. If separate service startup is needed, scripts MUST keep ports and process files explicit.

4. Use environment variables with documented defaults for runtime configuration.

   Rationale: environment variables keep deployment lightweight and avoid committing machine-specific files. The alternative was hard-coded script values, which would make deployment brittle across machines.

## Risks / Trade-offs

- Script behavior differs between Windows and Unix shells -> Start with Windows PowerShell scripts and document command contracts so Unix scripts can mirror them later.
- Frontend/backend integration path may need adjustment after implementation review -> Keep the deploy layout explicit and verify built assets are reachable through a documented health check.
- Long-running process management through scripts is less robust than a service manager -> Provide PID/log handling and make production service-manager integration a documented follow-up, not a hidden requirement.
- Missing local toolchain versions can make the script fail late -> Scripts MUST perform prerequisite checks before build or startup work begins.
