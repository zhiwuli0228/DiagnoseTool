import { describe, expect, it } from 'vitest';
import { DiagnosisProgressStatus, DiagnosisProgressStep, TaskAction, WorkflowStage } from './domain.js';
import { i18n } from './i18n.js';
import {
  actionTaskName,
  canStartTask,
  deriveConversationMessages,
  diagnosisReducer,
  initialWorkflowState
} from './workflow.js';

describe('workflow state', () => {
  it('transitions through session, multiple evidences, diagnosis, recovery, and completion', () => {
    let state = diagnosisReducer(initialWorkflowState, {
      type: 'sessionCreated',
      session: { id: 'INC-1', status: 'CREATED' }
    });
    state = diagnosisReducer(state, {
      type: 'evidencesSubmitted',
      evidences: [
        { id: 'EVD-1', type: 'LOG_SNIPPET', source: 'conversation' },
        { id: 'EVD-2', type: 'JSTACK', source: 'conversation' }
      ]
    });
    state = diagnosisReducer(state, { type: 'diagnosisStarted' });
    state = diagnosisReducer(state, {
      type: 'reportReceived',
      report: { id: 'RPT-1', summary: 'Redis pool exhausted', confidence: 'HIGH' }
    });
    state = diagnosisReducer(state, {
      type: 'recoveryReceived',
      actions: [{ id: 'ACT-1', title: 'Check Redis' }]
    });
    state = diagnosisReducer(state, {
      type: 'cardReceived',
      card: { id: 'CARD-1', markdown: '# Review' }
    });

    expect(state.stage).toBe(WorkflowStage.COMPLETED);
    expect(state.submittedEvidence).toHaveLength(2);
    expect(deriveConversationMessages(state).map((message) => message.id)).toEqual([
      'welcome',
      'session',
      'evidence-EVD-1',
      'evidence-EVD-2',
      'report',
      'recovery',
      'card'
    ]);
  });

  it('stores log analysis outputs without changing incident state', () => {
    const state = diagnosisReducer(initialWorkflowState, {
      type: 'logAnalysisReceived',
      session: { id: 'LOG-1', status: 'PROCESSED', fileSummaries: [{ sourceFile: 'app.log' }] },
      clusters: [{ clusterId: 'CLS-1' }],
      timeline: { events: [{ evidenceEventId: 'E1' }] }
    });

    expect(state.session).toBeNull();
    expect(state.logAnalysisSession.id).toBe('LOG-1');
    expect(state.logFileSummaries).toHaveLength(1);
    expect(state.logClusters).toHaveLength(1);
    expect(state.logTimeline).toHaveLength(1);
    expect(state.evidencePackMarkdown).toBe('');
  });

  it('stores bounded log search results for later extraction', () => {
    const state = diagnosisReducer(initialWorkflowState, {
      type: 'logSearchReceived',
      result: { totalMatched: 3, limit: 2, events: [{ id: 'LOG-E1' }] }
    });

    expect(state.logSearchResult.totalMatched).toBe(3);
    expect(state.logSearchResult.events).toHaveLength(1);
  });

  it('preserves drafts when a request fails', () => {
    const state = diagnosisReducer(initialWorkflowState, {
      type: 'failed',
      message: 'network error'
    });

    expect(state.stage).toBe(WorkflowStage.FAILED);
    expect(state.incidentDraft.title).toBe(initialWorkflowState.incidentDraft.title);
    expect(state.evidenceDraft.logContent).toBe(initialWorkflowState.evidenceDraft.logContent);
  });

  it('updates diagnosis progress without losing workflow data', () => {
    let state = diagnosisReducer(initialWorkflowState, {
      type: 'sessionCreated',
      session: { id: 'INC-1', status: 'CREATED' }
    });
    state = diagnosisReducer(state, { type: 'diagnosisStarted' });
    state = diagnosisReducer(state, {
      type: 'diagnosisProgressReceived',
      progress: {
        status: DiagnosisProgressStatus.RUNNING,
        percent: 70,
        step: DiagnosisProgressStep.GENERATING_REPORT,
        message: 'Generating diagnosis report.'
      }
    });
    state = diagnosisReducer(state, { type: 'diagnosisProgressWarning', message: 'progress warning' });

    expect(state.session.id).toBe('INC-1');
    expect(state.diagnosisProgress.percent).toBe(70);
    expect(state.diagnosisProgress.step).toBe(DiagnosisProgressStep.GENERATING_REPORT);
    expect(state.diagnosisProgress.warning).toBe('progress warning');
  });

  it('marks diagnosis progress failed independently from workflow failure', () => {
    const state = diagnosisReducer(initialWorkflowState, {
      type: 'diagnosisProgressFailed',
      message: 'bad json'
    });

    expect(state.diagnosisProgress.status).toBe(DiagnosisProgressStatus.FAILED);
    expect(state.diagnosisProgress.errorMessage).toBe('bad json');
  });
});

describe('workflow concurrency helpers', () => {
  it('creates stable observable task names', () => {
    expect(actionTaskName(TaskAction.RUN_DIAGNOSIS, 'INC-1')).toBe('diagnosis-run:INC-1');
  });

  it('blocks duplicate running tasks', () => {
    expect(canStartTask({
      taskName: 'diagnosis-run:INC-1',
      runningTasks: ['diagnosis-run:INC-1'],
      lastStartedAt: {},
      now: 1000,
      limitMs: 0
    })).toEqual({ allowed: false, reason: i18n.t('conversation.duplicateTask') });
  });

  it('applies configurable rate limits', () => {
    expect(canStartTask({
      taskName: 'diagnosis-run:INC-1',
      runningTasks: [],
      lastStartedAt: { 'diagnosis-run:INC-1': 900 },
      now: 1000,
      limitMs: 200
    })).toEqual({ allowed: false, reason: i18n.t('conversation.rateLimited') });
  });
});
