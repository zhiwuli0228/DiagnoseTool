## Why

LLM connection details currently depend on backend service configuration, so changing `baseUrl`, `api-key`, or `model` requires operational changes and often a service restart. Operators need a fast frontend-driven way to adjust LLM settings during diagnosis while preserving backend defaults when no frontend override is configured.

## What Changes

- Add a frontend configuration UI for LLM settings including `baseUrl`, `api-key`, and `model`.
- Add backend APIs to read, update, clear, and apply frontend-provided LLM configuration.
- Make LLM requests use the frontend-configured values immediately when present, falling back to backend service configuration when absent.
- Treat frontend LLM configuration as runtime configuration, not as a replacement for secure backend defaults.
- Do not expose stored API keys in plaintext when reading configuration state.

## Capabilities

### New Capabilities
- `frontend-llm-configuration`: Defines runtime LLM configuration management from the frontend, including hot-effective overrides and backend fallback behavior.

### Modified Capabilities

## Impact

- Affected backend areas: LLM client creation/request flow, runtime configuration storage, REST API for configuration management, tests for fallback and override behavior.
- Affected frontend areas: settings UI, API client, local workflow state, validation and feedback for configuration updates.
- Security impact: API keys become user-entered runtime secrets and must be masked in read responses and handled without logging sensitive values.
- Operational impact: users can switch model provider, model, or API key without restarting the backend service.
