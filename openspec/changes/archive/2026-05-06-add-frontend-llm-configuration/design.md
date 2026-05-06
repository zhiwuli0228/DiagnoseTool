## Context

The backend currently constructs the OpenAI-compatible LLM client from Spring configuration such as `thread-doctor.llm.base-url`, `thread-doctor.llm.api-key`, and `thread-doctor.llm.model`. These values are normally supplied by `application*.yml` and environment variables, so changing them requires backend configuration changes and often a service restart.

The frontend is a diagnosis workspace where operators already drive the workflow. Adding runtime LLM settings there allows quick provider/model switching during diagnosis while keeping backend configuration as the default source of truth.

## Goals / Non-Goals

**Goals:**
- Allow operators to configure LLM `baseUrl`, `api-key`, and `model` from the frontend.
- Make frontend-provided values hot-effective for subsequent LLM requests without backend restart.
- Fall back to backend service configuration when no frontend value is configured.
- Mask API keys in read responses and avoid logging secrets.
- Provide clear frontend feedback for save, clear, and active-source state.

**Non-Goals:**
- Do not persist runtime LLM settings to a database.
- Do not build multi-user permission management or audit logs in this change.
- Do not expose the full backend API key to the frontend.
- Do not change diagnosis report schemas or prompt templates.

## Decisions

1. Store frontend-provided LLM settings in a backend runtime configuration service.

   Rationale: request-time backend state is required because browser-only state would not affect server-side LLM calls unless every diagnosis request carried credentials. Backend runtime storage also avoids repeatedly sending API keys with each diagnosis operation. The trade-off is that settings are process-local and reset on backend restart.

2. Resolve effective LLM settings per LLM request.

   Rationale: reading the latest runtime settings before each LLM call makes changes hot-effective for subsequent requests without rebuilding the Spring bean or restarting the service. The alternative is recreating the LLM client bean, which is more complex and risks lifecycle issues.

3. Treat each configured field as an override with backend fallback.

   Rationale: users may only need to change one value such as `model`. Empty or absent frontend fields should not erase backend defaults. The effective configuration is built by using frontend non-blank values first, then backend properties.

4. Mask API key values in read APIs.

   Rationale: the frontend needs to know whether an API key is configured, but returning the full secret creates avoidable leakage. The API should return presence and a masked preview only.

## Risks / Trade-offs

- Runtime configuration resets on restart -> Document that backend `application*.yml` and environment variables remain the durable defaults.
- Process-local settings do not synchronize across multiple backend instances -> Keep scope to single-node/lightweight deployment and leave distributed config as a future capability.
- API key can still be entered from the browser -> Avoid plaintext readback and logging; rely on deployment access controls for who can use the settings UI.
- Invalid LLM configuration can break diagnosis requests -> Validate required fields and expose clear failure messages before or during save.
