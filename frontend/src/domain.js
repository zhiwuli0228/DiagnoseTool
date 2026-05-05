export const WorkflowStage = Object.freeze({
  IDLE: 'idle',
  COLLECTING_EVIDENCE: 'collectingEvidence',
  DIAGNOSING: 'diagnosing',
  REVIEWING_REPORT: 'reviewingReport',
  RECOMMENDING_RECOVERY: 'recommendingRecovery',
  COMPLETED: 'completed',
  FAILED: 'failed'
});

export const EvidenceType = Object.freeze({
  LOG_SNIPPET: 'LOG_SNIPPET',
  JSTACK: 'JSTACK'
});

export const SeverityLevel = Object.freeze({
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
  CRITICAL: 'CRITICAL'
});

export const ConversationRole = Object.freeze({
  ASSISTANT: 'assistant',
  USER: 'user',
  SYSTEM: 'system'
});

export const DiagnosisProgressStatus = Object.freeze({
  NOT_STARTED: 'NOT_STARTED',
  RUNNING: 'RUNNING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED'
});

export const DiagnosisProgressStep = Object.freeze({
  PENDING: 'PENDING',
  STARTED: 'STARTED',
  BUILDING_CONTEXT: 'BUILDING_CONTEXT',
  DETECTING_PATTERNS: 'DETECTING_PATTERNS',
  GENERATING_REPORT: 'GENERATING_REPORT',
  VALIDATING_REPORT: 'VALIDATING_REPORT',
  PERSISTING_REPORT: 'PERSISTING_REPORT',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED'
});

export const TaskAction = Object.freeze({
  CREATE_INCIDENT: 'incident-create',
  UPLOAD_EVIDENCE: 'evidence-upload',
  UPLOAD_METRICS: 'metrics-upload',
  RUN_DIAGNOSIS: 'diagnosis-run',
  GENERATE_RECOVERY: 'recovery-generate',
  EXECUTE_RECOVERY: 'recovery-execute',
  GENERATE_CARD: 'incident-card-generate',
  LOG_ZIP_UPLOAD: 'log-zip-upload',
  LOG_DIRECTORY_SCAN: 'log-directory-scan',
  LOG_SEARCH: 'log-search',
  LOG_CODEX_TASK: 'log-codex-task',
  LOG_OPENSPEC_DRAFT: 'log-openspec-draft'
});
