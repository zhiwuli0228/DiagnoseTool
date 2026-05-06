## ADDED Requirements

### Requirement: Unified Project Build Script
The system SHALL provide a repository-level build script that packages both the backend Spring Boot service and the frontend Vite application with one documented command.

#### Scenario: Successful full build
- **WHEN** the operator runs the documented full build command from the repository root
- **THEN** the system MUST produce a backend jar artifact and frontend static build assets without requiring manual per-module build steps

#### Scenario: Missing build prerequisite
- **WHEN** a required local tool such as Java, Maven, Node.js, or npm is unavailable
- **THEN** the build script MUST stop with a clear message identifying the missing prerequisite

### Requirement: Deployable Runtime Assembly
The system SHALL provide a deployment script that assembles a lightweight runtime directory for the application.

#### Scenario: Deployment package assembly
- **WHEN** the operator runs the documented deployment command after a successful build
- **THEN** the system MUST create a deployable directory containing the backend artifact, frontend assets or frontend service assets, runtime configuration examples, log directory, and start/stop helpers

#### Scenario: Repeat deployment
- **WHEN** the operator runs the deployment command multiple times
- **THEN** the system MUST refresh generated runtime artifacts predictably without requiring manual cleanup of previous build outputs

#### Scenario: Deploy existing build outputs
- **WHEN** the operator runs the deployment command with rebuild disabled after backend and frontend artifacts already exist
- **THEN** the system MUST assemble the deployable directory from the existing jar and frontend dist without invoking Maven or npm build steps

#### Scenario: Deploy explicit artifact paths
- **WHEN** the operator provides explicit backend jar and frontend dist paths to the deployment command
- **THEN** the system MUST use those paths as the deployment source artifacts

### Requirement: Application Startup Scripts
The system SHALL provide scripts to start the deployed application quickly, covering backend service startup and frontend access.

#### Scenario: Start deployed application
- **WHEN** the operator runs the documented startup command from the deployable directory
- **THEN** the backend service MUST start with configured ports and logs, and the frontend application MUST be available through the documented URL

#### Scenario: Stop deployed application
- **WHEN** the operator runs the documented stop command after startup
- **THEN** the scripts MUST stop the managed application process or processes without requiring the operator to manually locate process identifiers

### Requirement: Runtime Configuration Documentation
The system SHALL document the commands, environment variables, default ports, output directories, and verification steps for build, direct deployment, startup, and shutdown.

#### Scenario: Operator follows documentation
- **WHEN** an operator follows the documented workflow on a machine with required prerequisites
- **THEN** the operator MUST be able to build, deploy, start, verify, and stop the application using the provided commands

#### Scenario: Operator reads concise README
- **WHEN** an operator opens the repository README
- **THEN** the README MUST concisely explain the build, direct deploy, start, verify, and stop flow
