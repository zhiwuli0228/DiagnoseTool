import { ConversationRole, DiagnosisProgressStatus, DiagnosisProgressStep, TaskAction, WorkflowStage } from './domain.js';
import { i18n } from './i18n.js';

export const initialDiagnosisProgress = {
  status: DiagnosisProgressStatus.NOT_STARTED,
  percent: 0,
  step: DiagnosisProgressStep.PENDING,
  message: '',
  errorMessage: '',
  warning: ''
};

export const initialWorkflowState = {
  stage: WorkflowStage.IDLE,
  incidentDraft: {
    title: 'Redis timeout incident',
    description: 'Service reports Redis timeout and request latency increases.',
    severity: 'HIGH'
  },
  evidenceDraft: {
    logContent: 'redis.clients.jedis.exceptions.JedisConnectionException: Could not get a resource from the pool',
    jstackContent: ''
  },
  metricsDraft: {
    jvmMetricsJson: '',
    redisMetricsJson: '',
    kafkaMetricsJson: '',
    dbMetricsJson: ''
  },
  logAnalysisDraft: {
    sourceType: 'zip',
    zipFile: null,
    directoryFiles: [],
    searchKeywords: '',
    searchLevels: [],
    searchLimit: 20,
    includeStackTrace: true,
    ignoreCase: true,
    deduplicate: false,
    timeFrom: '',
    timeTo: ''
  },
  session: null,
  logAnalysisSession: null,
  logFileSummaries: [],
  logClusters: [],
  logTimeline: [],
  logSearchResult: null,
  evidencePack: null,
  evidencePackMarkdown: '',
  generatedCodexTask: null,
  generatedOpenSpecDraft: null,
  submittedEvidence: [],
  metricsSnapshots: [],
  report: null,
  recoveryActions: [],
  incidentCard: null,
  diagnosisProgress: initialDiagnosisProgress,
  runningTasks: [],
  error: ''
};

export function createTaskName(action, sessionId = 'draft') {
  return `${action}:${sessionId || 'draft'}`;
}

export function canStartTask({ taskName, runningTasks, lastStartedAt, now, limitMs }) {
  if (runningTasks.includes(taskName)) {
    return { allowed: false, reason: i18n.t('conversation.duplicateTask') };
  }
  const last = lastStartedAt[taskName] || 0;
  if (last > 0 && now - last < limitMs) {
    return { allowed: false, reason: i18n.t('conversation.rateLimited') };
  }
  return { allowed: true, reason: '' };
}

export function diagnosisReducer(state, action) {
  switch (action.type) {
    case 'incidentDraftChanged':
      return { ...state, incidentDraft: { ...state.incidentDraft, [action.field]: action.value } };
    case 'evidenceDraftChanged':
      return { ...state, evidenceDraft: { ...state.evidenceDraft, [action.field]: action.value } };
    case 'metricsDraftChanged':
      return { ...state, metricsDraft: { ...state.metricsDraft, [action.field]: action.value } };
    case 'logAnalysisDraftChanged':
      return { ...state, logAnalysisDraft: normalizeLogAnalysisDraft(state.logAnalysisDraft, action.field, action.value) };
    case 'taskStarted':
      return { ...state, runningTasks: [...state.runningTasks, action.taskName], error: '' };
    case 'taskFinished':
      return { ...state, runningTasks: state.runningTasks.filter((task) => task !== action.taskName) };
    case 'blocked':
      return { ...state, error: action.message };
    case 'failed':
      return { ...state, stage: WorkflowStage.FAILED, error: action.message };
    case 'sessionCreated':
      return { ...state, stage: WorkflowStage.COLLECTING_EVIDENCE, session: action.session, error: '' };
    case 'evidenceSubmitted':
      return { ...state, submittedEvidence: [...state.submittedEvidence, action.evidence], stage: WorkflowStage.COLLECTING_EVIDENCE, error: '' };
    case 'evidencesSubmitted':
      return { ...state, submittedEvidence: [...state.submittedEvidence, ...action.evidences], stage: WorkflowStage.COLLECTING_EVIDENCE, error: '' };
    case 'metricsSubmitted':
      return { ...state, metricsSnapshots: [...state.metricsSnapshots, action.snapshot], stage: WorkflowStage.COLLECTING_EVIDENCE, error: '' };
    case 'logAnalysisReceived':
      return {
        ...state,
        logAnalysisSession: action.session,
        logFileSummaries: action.session.fileSummaries || [],
        logClusters: action.clusters || [],
        logTimeline: action.timeline?.events || [],
        evidencePack: action.evidencePack || null,
        evidencePackMarkdown: action.evidencePackMarkdown || '',
        logSearchResult: null,
        error: ''
      };
    case 'logSearchReceived':
      return { ...state, logSearchResult: action.result, error: '' };
    case 'codexTaskReceived':
      return { ...state, generatedCodexTask: action.task, error: '' };
    case 'openSpecDraftReceived':
      return { ...state, generatedOpenSpecDraft: action.draft, error: '' };
    case 'diagnosisStarted':
      return { ...state, stage: WorkflowStage.DIAGNOSING, diagnosisProgress: {
        ...initialDiagnosisProgress,
        status: DiagnosisProgressStatus.RUNNING,
        percent: 10,
        step: DiagnosisProgressStep.STARTED,
        message: i18n.t('progress.messages.started')
      }, error: '' };
    case 'diagnosisProgressReset':
      return { ...state, diagnosisProgress: initialDiagnosisProgress };
    case 'diagnosisProgressReceived':
      return { ...state, diagnosisProgress: {
        ...state.diagnosisProgress,
        ...action.progress,
        warning: ''
      } };
    case 'diagnosisProgressWarning':
      return { ...state, diagnosisProgress: { ...state.diagnosisProgress, warning: action.message } };
    case 'diagnosisProgressFailed':
      return { ...state, diagnosisProgress: {
        ...state.diagnosisProgress,
        status: DiagnosisProgressStatus.FAILED,
        step: DiagnosisProgressStep.FAILED,
        errorMessage: action.message,
        warning: ''
      } };
    case 'reportReceived':
      return { ...state, stage: WorkflowStage.REVIEWING_REPORT, report: action.report, error: '' };
    case 'recoveryStarted':
      return { ...state, stage: WorkflowStage.RECOMMENDING_RECOVERY, error: '' };
    case 'recoveryReceived':
      return { ...state, stage: WorkflowStage.RECOMMENDING_RECOVERY, recoveryActions: action.actions, error: '' };
    case 'recoveryExecuted':
      return {
        ...state,
        recoveryActions: state.recoveryActions.map((item) => item.id === action.action.id ? action.action : item),
        error: ''
      };
    case 'cardReceived':
      return { ...state, stage: WorkflowStage.COMPLETED, incidentCard: action.card, error: '' };
    default:
      return state;
  }
}

function normalizeLogAnalysisDraft(current, field, value) {
  if (field === 'sourceType') {
    return {
      ...current,
      sourceType: value,
      zipFile: value === 'zip' ? current.zipFile : null,
      directoryFiles: value === 'directory' ? current.directoryFiles : []
    };
  }
  if (field === 'zipFile') {
    return { ...current, sourceType: 'zip', zipFile: value, directoryFiles: [] };
  }
  if (field === 'directoryFiles') {
    return { ...current, sourceType: 'directory', directoryFiles: value || [], zipFile: null };
  }
  return { ...current, [field]: value };
}

export function deriveConversationMessages(state, translator = i18n) {
  const messages = [{ id: 'welcome', role: ConversationRole.ASSISTANT, text: translator.t('conversation.welcome') }];
  if (state.session) {
    messages.push({
      id: 'session',
      role: ConversationRole.SYSTEM,
      text: translator.t('conversation.session', { id: state.session.id, status: state.session.status })
    });
  }
  for (const evidence of state.submittedEvidence) {
    messages.push({
      id: `evidence-${evidence.id}`,
      role: ConversationRole.USER,
      text: translator.t('conversation.evidence', {
        type: translator.label('evidenceType', evidence.type),
        source: evidence.source || 'manual'
      })
    });
  }
  if (state.metricsSnapshots.length > 0) {
    messages.push({ id: 'metrics', role: ConversationRole.USER, text: translator.t('conversation.metrics', { count: state.metricsSnapshots.length }) });
  }
  if (state.report) {
    messages.push({
      id: 'report',
      role: ConversationRole.ASSISTANT,
      text: translator.t('conversation.report', { summary: state.report.summary || translator.t('conversation.reportFallback') })
    });
  }
  if (state.recoveryActions.length > 0) {
    messages.push({ id: 'recovery', role: ConversationRole.ASSISTANT, text: translator.t('conversation.recovery', { count: state.recoveryActions.length }) });
  }
  if (state.incidentCard) {
    messages.push({ id: 'card', role: ConversationRole.SYSTEM, text: translator.t('conversation.card') });
  }
  if (state.error) {
    messages.push({ id: 'error', role: ConversationRole.SYSTEM, text: state.error });
  }
  return messages;
}

export function actionTaskName(action, sessionId) {
  return createTaskName(action, sessionId);
}

export { TaskAction };
