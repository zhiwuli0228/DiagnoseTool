## 1. Script Contract and Layout

- [x] 1.1 Define the repository-level script directory, command names, deploy output directory, and runtime file layout.
- [x] 1.2 Add prerequisite checks for Java, Maven, Node.js, and npm with clear failure messages.

## 2. Build Automation

- [x] 2.1 Implement the full build script that runs backend Maven packaging from the repository root.
- [x] 2.2 Extend the full build script to install or verify frontend dependencies and run the frontend Vite build.
- [x] 2.3 Ensure build output paths are deterministic and documented for later deployment assembly.

## 3. Deployment Assembly

- [x] 3.1 Implement the deployment script that refreshes the lightweight runtime directory.
- [x] 3.2 Copy the backend jar, frontend static assets, configuration examples, logs directory, and helper scripts into the runtime directory.
- [x] 3.3 Make repeated deployment runs refresh generated artifacts without requiring manual cleanup.

## 4. Runtime Startup and Shutdown

- [x] 4.1 Implement startup script behavior for configured ports, environment variables, logs, and process tracking.
- [x] 4.2 Implement stop script behavior that stops tracked application process or processes.
- [x] 4.3 Add a verification command or documented health check URL for confirming the application is running.

## 5. Documentation and Validation

- [x] 5.1 Document build, deploy, start, stop, configuration, default ports, output directories, and verification workflow.
- [x] 5.2 Validate the scripts on a clean workspace path with existing Maven and npm toolchains.
- [x] 5.3 Run backend and frontend tests or builds needed to verify no existing behavior regressed.

## 6. Direct Artifact Deployment

- [x] 6.1 Add deploy script parameters for explicit backend jar and frontend dist paths.
- [x] 6.2 Add a concise root README covering build, direct deploy, start, verify, and stop.
- [x] 6.3 Validate direct deployment from existing compiled artifacts.
