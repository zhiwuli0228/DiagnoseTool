function readBoolean(value, fallback) {
  if (value === undefined || value === null || value === '') {
    return fallback;
  }
  return String(value).toLowerCase() !== 'false';
}

function readNumber(value, fallback) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
}

export function createFrontendConfig(env = import.meta.env) {
  return {
    conversationalDiagnosisEnabled: readBoolean(env.VITE_ENABLE_CONVERSATIONAL_DIAGNOSIS, true),
    requestLimitMs: readNumber(env.VITE_DIAGNOSIS_REQUEST_LIMIT_MS, 700),
    diagnosisProgressPollMs: readNumber(env.VITE_DIAGNOSIS_PROGRESS_POLL_MS, 1000)
  };
}

export const frontendConfig = createFrontendConfig();
