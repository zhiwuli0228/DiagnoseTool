## Why

The project currently has separate frontend and backend build entry points, but lacks a lightweight, repeatable workflow for packaging, deploying, and starting the full application quickly. A unified script set will reduce manual setup steps and make local or small-server deployment predictable.

## What Changes

- Add repository-level build scripts that package the backend Spring Boot service and frontend Vite application in one command.
- Add deployment scripts that assemble a lightweight runnable distribution containing the backend artifact, frontend static assets, configuration examples, and start/stop helpers.
- Support deploying already-built artifacts directly without rebuilding, including explicit backend jar and frontend dist paths.
- Add startup scripts for launching backend and frontend-facing services with clear environment variables and sensible defaults.
- Document the expected usage flow for build, direct deploy, start, stop, and verification in a concise README.

## Capabilities

### New Capabilities
- `project-build-scripts`: Defines the required behavior for lightweight project build, deployment assembly, and application startup scripts covering both frontend and backend services.

### Modified Capabilities

## Impact

- Affected areas: repository scripts, backend Maven packaging, frontend npm build output, deployment directory layout, runtime configuration documentation.
- External dependencies: existing Java 21, Maven, Node.js/npm toolchain; no new runtime service dependency is expected.
- Operational impact: developers and operators can produce and run a deployable application package through documented commands instead of manual per-module steps.
