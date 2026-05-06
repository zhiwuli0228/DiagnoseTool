## 1. Backend Runtime Configuration

- [x] 1.1 Add backend DTOs for LLM configuration read, save, clear, and effective-source responses.
- [x] 1.2 Implement a runtime LLM configuration service that stores frontend overrides in memory and resolves backend fallback values.
- [x] 1.3 Add validation for URL format, blank override values, and required effective configuration fields.

## 2. Backend API

- [x] 2.1 Add REST endpoints to read, save, and clear frontend-provided LLM configuration.
- [x] 2.2 Ensure read responses mask API key values and only expose configured status or a safe preview.
- [x] 2.3 Add backend API tests for save, read, clear, validation failures, and API key masking.

## 3. LLM Client Hot Configuration

- [x] 3.1 Refactor the OpenAI-compatible client to resolve effective LLM settings at request time.
- [x] 3.2 Ensure frontend overrides take effect on the next LLM request without backend restart.
- [x] 3.3 Ensure missing frontend fields fall back to backend service configuration.
- [x] 3.4 Add LLM client tests for default fallback, partial override, full override, and cleared override behavior.

## 4. Frontend Settings Experience

- [x] 4.1 Add frontend API functions for reading, saving, and clearing LLM configuration.
- [x] 4.2 Add an LLM settings UI with fields for `baseUrl`, `api-key`, and `model`.
- [x] 4.3 Show active source, masked API key status, save success, clear success, and validation errors.
- [x] 4.4 Ensure saved settings do not require a page reload and are used by subsequent diagnosis actions.

## 5. Validation and Documentation

- [x] 5.1 Update user-facing documentation or README notes for frontend LLM configuration behavior and backend fallback.
- [x] 5.2 Run backend tests covering runtime LLM configuration and existing diagnosis behavior.
- [x] 5.3 Run frontend tests covering settings UI behavior and existing diagnosis workflow.
