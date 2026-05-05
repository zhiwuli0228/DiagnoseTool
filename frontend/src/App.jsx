import React, { useEffect, useMemo, useReducer, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  Activity,
  ClipboardList,
  Copy,
  Download,
  FileText,
  FolderOpen,
  MessageSquare,
  RotateCw,
  ShieldCheck,
  Stethoscope,
  Upload
} from 'lucide-react';
import { frontendConfig } from './config.js';
import { diagnosisApi } from './diagnosisApi.js';
import { DiagnosisProgressStep, EvidenceType, SeverityLevel, TaskAction, WorkflowStage } from './domain.js';
import { i18n } from './i18n.js';
import {
  actionTaskName,
  canStartTask,
  deriveConversationMessages,
  diagnosisReducer,
  initialWorkflowState
} from './workflow.js';
import './styles.css';

const LOG_LEVEL_OPTIONS = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'];

function Tip({ text }) {
  return <span className="tip">{text}</span>;
}

function formatLogSearchAsSnippet({ session, searchResult }) {
  const lines = [
    '[日志检索提取的日志片段]',
    `sessionId=${session.id}`,
    `status=${session.status || 'UNKNOWN'}`,
    `matched=${searchResult.totalMatched}`,
    `limit=${searchResult.limit}`
  ];
  if (searchResult.events?.length > 0) {
    lines.push('', 'matched events:');
    searchResult.events.slice(0, searchResult.limit || 20).forEach((event) => {
      lines.push(`- ${event.timestamp || 'unknown time'} [${event.level}] ${event.sourceFile}:${event.lineNumber}`);
      if (event.traceId) {
        lines.push(`  traceId=${event.traceId}`);
      }
      if (event.exceptionType) {
        lines.push(`  exception=${event.exceptionType}`);
      }
      if (event.duplicateCount > 1) {
        lines.push(`  duplicateCount=${event.duplicateCount}`);
      }
      lines.push(`  message=${event.message || event.rawText || ''}`);
      if (event.stackTrace) {
        lines.push(`  stackTrace=${String(event.stackTrace).slice(0, 1200)}`);
      }
    });
  }
  return lines.join('\n').slice(0, 12000);
}

function visibleFileSummaries(files) {
  return files.slice(0, 8);
}

function clusterTitle(cluster) {
  return cluster.exceptionType
    || cluster.sampleLogs?.[0]
    || cluster.suspectedClasses?.[0]
    || cluster.fingerprint
    || cluster.clusterId;
}

function clusterMeta(cluster) {
  return `${cluster.severity} / ${cluster.count}`;
}

function timelineSummary(event) {
  const source = event.sourceFile ? `${event.sourceFile}${event.threadName ? ` / ${event.threadName}` : ''}` : '';
  return [event.summary, source].filter(Boolean).join(' - ');
}

function DiagnosisProgressBar({ progress }) {
  const percent = Math.max(0, Math.min(100, Number(progress.percent || 0)));
  const stepLabel = i18n.progressStep(progress.step);
  const statusLabel = i18n.progressStatus(progress.status);
  const flow = [
    { label: i18n.t('progress.flow.prepare'), steps: [DiagnosisProgressStep.PENDING, DiagnosisProgressStep.STARTED] },
    { label: i18n.t('progress.flow.analyzeEvidence'), steps: [DiagnosisProgressStep.BUILDING_CONTEXT, DiagnosisProgressStep.DETECTING_PATTERNS] },
    { label: i18n.t('progress.flow.requestModel'), steps: [DiagnosisProgressStep.GENERATING_REPORT] },
    { label: i18n.t('progress.flow.analyzeResult'), steps: [DiagnosisProgressStep.VALIDATING_REPORT, DiagnosisProgressStep.PERSISTING_REPORT] },
    { label: i18n.t('progress.flow.completed'), steps: [DiagnosisProgressStep.COMPLETED] }
  ];
  const currentFlowIndex = Math.max(0, flow.findIndex((item) => item.steps.includes(progress.step)));

  return (
    <section className="progress-panel" aria-label={i18n.t('progress.title')}>
      <div className="progress-header">
        <strong>{i18n.t('progress.title')}</strong>
        <span>{statusLabel} - {i18n.t('progress.percent', { percent })}</span>
      </div>
      <ol className="progress-flow" aria-label={i18n.t('progress.flowLabel')}>
        {flow.map((item, index) => {
          const state = index < currentFlowIndex ? 'completed' : index === currentFlowIndex ? 'active' : 'pending';
          return (
            <li key={item.label} className={`progress-flow-item ${state}`}>
              <span>{index + 1}</span>
              <strong>{item.label}</strong>
            </li>
          );
        })}
      </ol>
      <div className="progress-track" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow={percent} aria-label={i18n.t('progress.title')}>
        <span className="progress-fill" style={{ width: `${percent}%` }} />
      </div>
      <p className="progress-current">{i18n.t('progress.current', { step: stepLabel })}{progress.message ? ` - ${progress.message}` : ''}</p>
      {progress.warning && <p className="progress-warning">{progress.warning}</p>}
      {progress.errorMessage && <p className="progress-warning">{progress.errorMessage}</p>}
    </section>
  );
}

export function ConversationalDiagnosisApp({ apiClient = diagnosisApi, config = frontendConfig, now = () => Date.now() }) {
  const [state, dispatch] = useReducer(diagnosisReducer, initialWorkflowState);
  const [copyStatus, setCopyStatus] = useState('');
  const [expandedEvidenceInputs, setExpandedEvidenceInputs] = useState({ log: false, jstack: false });
  const lastStartedAt = useRef({});
  const runningTasksRef = useRef(new Set());
  const progressTimerRef = useRef(null);
  const messages = useMemo(() => deriveConversationMessages(state, i18n), [state]);
  const sessionId = state.session?.id;

  function stopDiagnosisProgressPolling() {
    if (progressTimerRef.current) {
      clearInterval(progressTimerRef.current);
      progressTimerRef.current = null;
    }
  }

  async function fetchDiagnosisProgress(activeSessionId) {
    try {
      const progress = await apiClient.getDiagnosisProgress(activeSessionId);
      dispatch({ type: 'diagnosisProgressReceived', progress });
    } catch {
      dispatch({ type: 'diagnosisProgressWarning', message: i18n.t('progress.warning') });
    }
  }

  function startDiagnosisProgressPolling(activeSessionId) {
    stopDiagnosisProgressPolling();
    fetchDiagnosisProgress(activeSessionId);
    const intervalMs = Math.max(250, config.diagnosisProgressPollMs ?? 1000);
    progressTimerRef.current = setInterval(() => fetchDiagnosisProgress(activeSessionId), intervalMs);
  }

  useEffect(() => () => stopDiagnosisProgressPolling(), []);

  if (!config.conversationalDiagnosisEnabled) {
    return (
      <main className="app-shell">
        <header className="app-header">
          <Stethoscope size={28} />
          <div>
            <h1>{i18n.t('app.title')}</h1>
            <p>{i18n.t('app.disabled')}</p>
          </div>
        </header>
      </main>
    );
  }

  async function runGuarded(taskName, work) {
    const startedAt = now();
    const decision = canStartTask({
      taskName,
      runningTasks: Array.from(runningTasksRef.current),
      lastStartedAt: lastStartedAt.current,
      now: startedAt,
      limitMs: config.requestLimitMs
    });
    if (!decision.allowed) {
      dispatch({ type: 'blocked', message: decision.reason });
      return;
    }

    // 任务锁和限流时间放在 ref 中，避免重复点击在渲染前穿透。
    lastStartedAt.current[taskName] = startedAt;
    lastStartedAt.current[taskName] = startedAt;
    runningTasksRef.current.add(taskName);
    dispatch({ type: 'taskStarted', taskName });
    try {
      await work();
    } catch (error) {
      dispatch({ type: 'failed', message: error.message || i18n.t('conversation.requestFailed') });
    } finally {
      runningTasksRef.current.delete(taskName);
      dispatch({ type: 'taskFinished', taskName });
    }
  }

  const updateIncidentDraft = (field, value) => dispatch({ type: 'incidentDraftChanged', field, value });
  const updateEvidenceDraft = (field, value) => dispatch({ type: 'evidenceDraftChanged', field, value });
  const updateMetricsDraft = (field, value) => dispatch({ type: 'metricsDraftChanged', field, value });
  const updateLogAnalysisDraft = (field, value) => dispatch({ type: 'logAnalysisDraftChanged', field, value });

  function toggleEvidenceInput(name) {
    setExpandedEvidenceInputs((current) => ({ ...current, [name]: !current[name] }));
  }

  function createIncident() {
    return runGuarded(actionTaskName(TaskAction.CREATE_INCIDENT, 'draft'), async () => {
      dispatch({ type: 'sessionCreated', session: await apiClient.createIncident(state.incidentDraft) });
    });
  }

  function uploadEvidence() {
    return runGuarded(actionTaskName(TaskAction.UPLOAD_EVIDENCE, sessionId), async () => {
      const drafts = [
        { type: EvidenceType.LOG_SNIPPET, content: state.evidenceDraft.logContent },
        { type: EvidenceType.JSTACK, content: state.evidenceDraft.jstackContent }
      ].filter((item) => item.content?.trim());
      if (drafts.length === 0) {
        throw new Error(i18n.t('tips.evidenceContent'));
      }
      const evidences = await Promise.all(drafts.map((item) => apiClient.uploadEvidence(sessionId, {
        type: item.type,
        source: 'conversation',
        content: item.content.trim()
      })));
      dispatch({ type: 'evidencesSubmitted', evidences });
    });
  }

  function uploadMetrics() {
    return runGuarded(actionTaskName(TaskAction.UPLOAD_METRICS, sessionId), async () => {
      dispatch({ type: 'metricsSubmitted', snapshot: await apiClient.uploadMetrics(sessionId, state.metricsDraft) });
    });
  }

  async function loadLogAnalysisResults(logSession) {
    const [clusters, timeline] = await Promise.all([
      apiClient.getLogClusters(logSession.id),
      apiClient.getLogTimeline(logSession.id)
    ]);
    dispatch({ type: 'logAnalysisReceived', session: logSession, clusters, timeline });
    return { session: logSession, clusters, timeline };
  }

  async function prepareLogAnalysis(logSession) {
    await loadLogAnalysisResults(logSession);
    setExpandedEvidenceInputs((current) => ({ ...current, log: true }));
  }

  function uploadLogZip() {
    return runGuarded(actionTaskName(TaskAction.LOG_ZIP_UPLOAD, state.logAnalysisDraft.zipFile?.name || 'zip'), async () => {
      if (!state.logAnalysisDraft.zipFile) {
        throw new Error(i18n.t('tips.logZip'));
      }
      const logSession = await apiClient.createLogAnalysisSession();
      await prepareLogAnalysis(await apiClient.uploadLogZip(logSession.id, state.logAnalysisDraft.zipFile));
    });
  }

  function scanLogDirectory() {
    return runGuarded(actionTaskName(TaskAction.LOG_DIRECTORY_SCAN, 'directory'), async () => {
      if (!state.logAnalysisDraft.directoryFiles.length) {
        throw new Error(i18n.t('tips.logDirectory'));
      }
      const logSession = await apiClient.createLogAnalysisSession();
      await prepareLogAnalysis(await apiClient.uploadLogDirectory(logSession.id, state.logAnalysisDraft.directoryFiles));
    });
  }

  function searchLogEvents() {
    return runGuarded(actionTaskName(TaskAction.LOG_SEARCH, state.logAnalysisSession?.id), async () => {
      if (!state.logAnalysisSession) {
        throw new Error(i18n.t('tips.logSearchRequired'));
      }
      if (!state.logAnalysisDraft.searchKeywords.trim()) {
        throw new Error(i18n.t('tips.logSearchKeywords'));
      }
      const levels = state.logAnalysisDraft.searchLevels;
      const result = await apiClient.searchLogEvents(state.logAnalysisSession.id, {
        keywords: state.logAnalysisDraft.searchKeywords.trim(),
        levels,
        timeFrom: state.logAnalysisDraft.timeFrom || null,
        timeTo: state.logAnalysisDraft.timeTo || null,
        limit: Number(state.logAnalysisDraft.searchLimit) || 20,
        includeStackTrace: Boolean(state.logAnalysisDraft.includeStackTrace),
        ignoreCase: Boolean(state.logAnalysisDraft.ignoreCase),
        deduplicate: Boolean(state.logAnalysisDraft.deduplicate)
      });
      dispatch({ type: 'logSearchReceived', result });
    });
  }

  function extractLogSearchToSnippet() {
    if (!state.logAnalysisSession || !state.logSearchResult) {
      dispatch({ type: 'blocked', message: i18n.t('tips.logSearchRequired') });
      return;
    }
    dispatch({
      type: 'evidenceDraftChanged',
      field: 'logContent',
      value: formatLogSearchAsSnippet({ session: state.logAnalysisSession, searchResult: state.logSearchResult })
    });
  }

  function runDiagnosis() {
    return runGuarded(actionTaskName(TaskAction.RUN_DIAGNOSIS, sessionId), async () => {
      dispatch({ type: 'diagnosisStarted' });
      startDiagnosisProgressPolling(sessionId);
      try {
        dispatch({ type: 'reportReceived', report: await apiClient.runDiagnosis(sessionId) });
      } catch (error) {
        dispatch({ type: 'diagnosisProgressFailed', message: error.message || i18n.t('conversation.requestFailed') });
        throw error;
      } finally {
        stopDiagnosisProgressPolling();
      }
    });
  }

  function generateRecoveryActions() {
    return runGuarded(actionTaskName(TaskAction.GENERATE_RECOVERY, sessionId), async () => {
      dispatch({ type: 'recoveryStarted' });
      dispatch({ type: 'recoveryReceived', actions: await apiClient.generateRecoveryActions(sessionId) });
    });
  }

  function simulateRecoveryAction(actionId) {
    return runGuarded(actionTaskName(TaskAction.EXECUTE_RECOVERY, `${sessionId}:${actionId}`), async () => {
      dispatch({ type: 'recoveryExecuted', action: await apiClient.simulateRecoveryAction(sessionId, actionId) });
    });
  }

  function generateIncidentCard() {
    return runGuarded(actionTaskName(TaskAction.GENERATE_CARD, sessionId), async () => {
      dispatch({ type: 'cardReceived', card: await apiClient.generateIncidentCard(sessionId) });
      setCopyStatus('');
    });
  }

  async function copyIncidentCard() {
    const markdown = state.incidentCard?.markdown || '';
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(markdown);
    }
    setCopyStatus(i18n.t('conversation.copied'));
  }

  function downloadIncidentCard() {
    const markdown = state.incidentCard?.markdown || '';
    const blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${sessionId || 'incident'}-result.md`;
    link.click();
    URL.revokeObjectURL(url);
  }

  const hasSession = Boolean(sessionId);
  const hasReport = Boolean(state.report);
  const isBusy = state.runningTasks.length > 0;
  const hasEvidenceDraft = Boolean(state.evidenceDraft.logContent?.trim() || state.evidenceDraft.jstackContent?.trim());
  const showDiagnosisProgress = state.stage === WorkflowStage.DIAGNOSING || state.diagnosisProgress.status === 'FAILED';

  return (
    <main className="app-shell">
      <header className="app-header">
        <Stethoscope size={30} />
        <div>
          <h1>{i18n.t('app.title')}</h1>
          <p>{i18n.t('app.subtitle')}</p>
        </div>
      </header>

      <section className="workspace">
        <aside className="conversation-panel" aria-label={i18n.t('app.conversation')}>
          <div className="panel-title"><MessageSquare size={18} /><h2>{i18n.t('app.conversation')}</h2></div>
          <ol className="message-list">
            {messages.map((message) => (
              <li key={message.id} className={`message ${message.role}`}>
                <span>{message.role}</span>
                <p>{message.text}</p>
              </li>
            ))}
          </ol>
        </aside>

        <div className="control-stack">
          <section className="panel">
            <div className="panel-title"><Activity size={18} /><h2>{i18n.t('app.incident')}</h2></div>
            <label>{i18n.t('labels.title')}<input title={i18n.t('tips.title')} value={state.incidentDraft.title} onChange={(event) => updateIncidentDraft('title', event.target.value)} /></label>
            <Tip text={i18n.t('tips.title')} />
            <label>{i18n.t('labels.description')}<textarea title={i18n.t('tips.description')} value={state.incidentDraft.description} onChange={(event) => updateIncidentDraft('description', event.target.value)} /></label>
            <Tip text={i18n.t('tips.description')} />
            <label>{i18n.t('labels.severity')}<select title={i18n.t('tips.severity')} value={state.incidentDraft.severity} onChange={(event) => updateIncidentDraft('severity', event.target.value)}>{Object.values(SeverityLevel).map((level) => <option key={level} value={level}>{i18n.label('severity', level)}</option>)}</select></label>
            <button onClick={createIncident} disabled={isBusy} title={i18n.t('tips.startSession')}><Activity size={16} /> {i18n.t('buttons.startSession')}</button>
            {state.session && <div className="status-line"><span>{state.session.id}</span><strong>{i18n.label('stage', state.stage)}</strong></div>}
          </section>

          <section className="panel">
            <div className="panel-title"><FileText size={18} /><h2>{i18n.t('app.evidence')}</h2></div>
            <div className="collapsible-input">
              <button type="button" className="secondary-button" aria-expanded={expandedEvidenceInputs.log} onClick={() => toggleEvidenceInput('log')} title={i18n.t('tips.logSnippetContent')}><FileText size={16} /> {i18n.t('labels.logSnippetContent')}</button>
              {expandedEvidenceInputs.log && (
                <>
                  <div className="log-source-controls">
                    <label className="checkbox-label"><input type="radio" name="log-source" checked={state.logAnalysisDraft.sourceType === 'zip'} onChange={() => updateLogAnalysisDraft('sourceType', 'zip')} />{i18n.t('labels.logSourceZip')}</label>
                    <label className="checkbox-label"><input type="radio" name="log-source" checked={state.logAnalysisDraft.sourceType === 'directory'} onChange={() => updateLogAnalysisDraft('sourceType', 'directory')} />{i18n.t('labels.logSourceDirectory')}</label>
                    {state.logAnalysisDraft.sourceType === 'zip' && (
                      <>
                        <label>{i18n.t('labels.logZip')}<input type="file" accept=".zip,application/zip" title={i18n.t('tips.logZip')} onChange={(event) => updateLogAnalysisDraft('zipFile', event.target.files?.[0] || null)} /></label>
                        <Tip text={i18n.t('tips.logZip')} />
                        <button onClick={uploadLogZip} disabled={isBusy || !state.logAnalysisDraft.zipFile} title={i18n.t('tips.uploadLogZip')}><Upload size={16} /> {i18n.t('buttons.uploadLogZip')}</button>
                        <Tip text={i18n.t('tips.uploadLogZip')} />
                      </>
                    )}
                    {state.logAnalysisDraft.sourceType === 'directory' && (
                      <>
                        <label>{i18n.t('labels.logDirectory')}<input type="file" multiple webkitdirectory="" directory="" title={i18n.t('tips.logDirectory')} onChange={(event) => updateLogAnalysisDraft('directoryFiles', Array.from(event.target.files || []))} /></label>
                        <Tip text={i18n.t('tips.logDirectory')} />
                        <button onClick={scanLogDirectory} disabled={isBusy || state.logAnalysisDraft.directoryFiles.length === 0} title={i18n.t('tips.scanLogDirectory')}><FolderOpen size={16} /> {i18n.t('buttons.scanLogDirectory')}</button>
                        <Tip text={i18n.t('tips.scanLogDirectory')} />
                      </>
                    )}
                  </div>
                  {state.logAnalysisSession && <div className="status-line"><span>{i18n.t('labels.logSession')}: {state.logAnalysisSession.id}</span><strong>{state.logAnalysisSession.status}</strong></div>}
                  {state.logFileSummaries.length > 0 && <article className="result-block"><h3>{i18n.t('labels.logFiles')}</h3><p>{i18n.t('labels.logFileSummary', { shown: visibleFileSummaries(state.logFileSummaries).length, total: state.logFileSummaries.length })}</p><div className="compact-list">{visibleFileSummaries(state.logFileSummaries).map((file) => <div key={file.sourceFile}><strong>{file.sourceFile}</strong><span>{file.eventCount} events / {file.unparsedCount} unparsed</span></div>)}</div></article>}
                  {state.logClusters.length > 0 && <article className="result-block"><h3>{i18n.t('labels.logClusters')}</h3><div className="compact-list">{state.logClusters.slice(0, 8).map((cluster) => <div key={cluster.clusterId}><strong>{clusterTitle(cluster)}</strong><span>{clusterMeta(cluster)}</span></div>)}</div></article>}
                  {state.logTimeline.length > 0 && <article className="result-block"><h3>{i18n.t('labels.logTimeline')}</h3><div className="compact-list timeline-list">{state.logTimeline.slice(0, 10).map((event) => <div key={`${event.evidenceEventId}-${event.time}`}><strong>{event.severity}</strong><span>{timelineSummary(event)}</span></div>)}</div></article>}
                  {state.logAnalysisSession && (
                    <div className="log-search-controls">
                      <label className="log-search-keywords">{i18n.t('labels.logSearchKeywords')}<textarea title={i18n.t('tips.logSearchKeywords')} value={state.logAnalysisDraft.searchKeywords} onChange={(event) => updateLogAnalysisDraft('searchKeywords', event.target.value)} /></label>
                      <Tip text={i18n.t('tips.logSearchKeywords')} />
                      <div className="log-search-filters">
                        <div className="log-search-row">
                          <label>{i18n.t('labels.logSearchLevels')}<select title={i18n.t('tips.logSearchLevels')} value={state.logAnalysisDraft.searchLevels[0] || ''} onChange={(event) => updateLogAnalysisDraft('searchLevels', event.target.value ? [event.target.value] : [])}><option value="">{i18n.t('labels.allLogLevels')}</option>{LOG_LEVEL_OPTIONS.map((level) => <option key={level} value={level}>{level}</option>)}</select></label>
                          <label>{i18n.t('labels.logSearchLimit')}<input type="number" min="1" max="100" title={i18n.t('tips.logSearchLimit')} value={state.logAnalysisDraft.searchLimit} onChange={(event) => updateLogAnalysisDraft('searchLimit', event.target.value)} /></label>
                        </div>
                        <div className="log-search-tips"><Tip text={i18n.t('tips.logSearchLevels')} /><Tip text={i18n.t('tips.logSearchLimit')} /></div>
                        <div className="log-search-row log-search-time-row">
                          <label>{i18n.t('labels.timeFrom')}<input type="datetime-local" step="1" title={i18n.t('tips.timeFrom')} value={state.logAnalysisDraft.timeFrom} onChange={(event) => updateLogAnalysisDraft('timeFrom', event.target.value)} /></label>
                          <label>{i18n.t('labels.timeTo')}<input type="datetime-local" step="1" title={i18n.t('tips.timeTo')} value={state.logAnalysisDraft.timeTo} onChange={(event) => updateLogAnalysisDraft('timeTo', event.target.value)} /></label>
                        </div>
                        <div className="log-search-tips"><Tip text={i18n.t('tips.timeFrom')} /><Tip text={i18n.t('tips.timeTo')} /></div>
                        <div className="log-search-row log-search-action-row">
                          <label className="checkbox-label"><input type="checkbox" checked={state.logAnalysisDraft.includeStackTrace} onChange={(event) => updateLogAnalysisDraft('includeStackTrace', event.target.checked)} />{i18n.t('labels.includeStackTrace')}</label>
                          <label className="checkbox-label"><input type="checkbox" checked={state.logAnalysisDraft.ignoreCase} onChange={(event) => updateLogAnalysisDraft('ignoreCase', event.target.checked)} />{i18n.t('labels.ignoreCase')}</label>
                          <label className="checkbox-label"><input type="checkbox" checked={state.logAnalysisDraft.deduplicate} onChange={(event) => updateLogAnalysisDraft('deduplicate', event.target.checked)} />{i18n.t('labels.deduplicateLogs')}</label>
                          <button onClick={searchLogEvents} disabled={isBusy || !state.logAnalysisDraft.searchKeywords.trim()} title={i18n.t('tips.searchLogEvents')}><FileText size={16} /> {i18n.t('buttons.searchLogEvents')}</button>
                        </div>
                        <div className="log-search-tips log-search-action-tips"><Tip text={i18n.t('tips.includeStackTrace')} /><Tip text={i18n.t('tips.ignoreCase')} /><Tip text={i18n.t('tips.deduplicateLogs')} /><Tip text={i18n.t('tips.searchLogEvents')} /></div>
                      </div>
                    </div>
                  )}
                  {state.logSearchResult && <article className="result-block"><h3>{i18n.t('labels.logSearchResults')}</h3><p>{i18n.t('labels.logSearchMatched', { count: state.logSearchResult.totalMatched, limit: state.logSearchResult.limit })}</p><div className="compact-list">{state.logSearchResult.events.map((event) => <div key={event.id}><strong>{event.exceptionType || event.level}{event.duplicateCount > 1 ? ` x${event.duplicateCount}` : ''}</strong><span>{event.message || event.rawText}</span></div>)}</div><button onClick={extractLogSearchToSnippet} disabled={isBusy || state.logSearchResult.events.length === 0} title={i18n.t('tips.extractLogSearch')}><FileText size={16} /> {i18n.t('buttons.extractLogSearch')}</button><Tip text={i18n.t('tips.extractLogSearch')} /></article>}
                  <label>{i18n.t('labels.logSnippetContent')}<textarea title={i18n.t('tips.logSnippetContent')} value={state.evidenceDraft.logContent} onChange={(event) => updateEvidenceDraft('logContent', event.target.value)} /></label>
                  <Tip text={i18n.t('tips.logSnippetContent')} />
                </>
              )}
            </div>
            <div className="collapsible-input">
              <button type="button" className="secondary-button" aria-expanded={expandedEvidenceInputs.jstack} onClick={() => toggleEvidenceInput('jstack')} title={i18n.t('tips.jstackContent')}><FileText size={16} /> {i18n.t('labels.jstackContent')}</button>
              {expandedEvidenceInputs.jstack && (
                <>
                  <label>{i18n.t('labels.jstackContent')}<textarea title={i18n.t('tips.jstackContent')} value={state.evidenceDraft.jstackContent} onChange={(event) => updateEvidenceDraft('jstackContent', event.target.value)} /></label>
                  <Tip text={i18n.t('tips.jstackContent')} />
                </>
              )}
            </div>
            <button onClick={uploadEvidence} disabled={!hasSession || isBusy || !hasEvidenceDraft} title={i18n.t('tips.submitEvidence')}><FileText size={16} /> {i18n.t('buttons.submitEvidence')}</button>
            <div className="metrics-grid">
              {['jvmMetricsJson', 'redisMetricsJson', 'kafkaMetricsJson', 'dbMetricsJson'].map((field) => <label key={field}>{i18n.t(`labels.${field}`)}<textarea title={i18n.t('tips.metricsJson')} value={state.metricsDraft[field]} onChange={(event) => updateMetricsDraft(field, event.target.value)} /></label>)}
            </div>
            <Tip text={i18n.t('tips.metricsJson')} />
            <button onClick={uploadMetrics} disabled={!hasSession || isBusy} title={i18n.t('tips.submitMetrics')}><ClipboardList size={16} /> {i18n.t('buttons.submitMetrics')}</button>
          </section>

          <section className="panel">
            <div className="panel-title"><ShieldCheck size={18} /><h2>{i18n.t('app.diagnosis')}</h2></div>
            <button onClick={runDiagnosis} disabled={!hasSession || isBusy} title={i18n.t('tips.runDiagnosis')}><Stethoscope size={16} /> {i18n.t('buttons.runDiagnosis')}</button>
            {showDiagnosisProgress && <DiagnosisProgressBar progress={state.diagnosisProgress} />}
            {state.report && <article className="result-block"><h3>{i18n.t('app.report')}</h3><p>{state.report.summary}</p><dl><dt>{i18n.t('labels.confidence')}</dt><dd>{i18n.label('confidence', state.report.confidence)}</dd></dl><pre>{state.report.reportJson || JSON.stringify(state.report, null, 2)}</pre></article>}
          </section>

          <section className="panel">
            <div className="panel-title"><RotateCw size={18} /><h2>{i18n.t('app.recovery')}</h2></div>
            <button onClick={generateRecoveryActions} disabled={!hasReport || isBusy} title={i18n.t('tips.generateRecovery')}><RotateCw size={16} /> {i18n.t('buttons.generateRecovery')}</button>
            <div className="action-list">
              {state.recoveryActions.map((action) => <article key={action.id} className="action-card"><h3>{action.title}</h3><p>{action.description}</p><dl><dt>{i18n.t('labels.risk')}</dt><dd>{i18n.label('risk', action.riskLevel)}</dd><dt>{i18n.t('labels.approval')}</dt><dd>{String(action.needApproval)}</dd><dt>{i18n.t('labels.verification')}</dt><dd>{action.verification}</dd></dl>{action.executionResult && <p className="execution-result">{action.executionResult}</p>}<button onClick={() => simulateRecoveryAction(action.id)} disabled={isBusy} title={i18n.t('tips.simulate')}>{i18n.t('buttons.simulate')}</button></article>)}
            </div>
            <button onClick={generateIncidentCard} disabled={state.recoveryActions.length === 0 || isBusy} title={i18n.t('tips.generateIncidentCard')}>{i18n.t('buttons.generateIncidentCard')}</button>
            {state.incidentCard && <article className="result-block"><h3>{i18n.t('app.incidentCard')}</h3><div className="document-actions"><button onClick={copyIncidentCard} title={i18n.t('tips.copyDocument')}><Copy size={16} /> {i18n.t('buttons.copyDocument')}</button><button onClick={downloadIncidentCard} title={i18n.t('tips.downloadDocument')}><Download size={16} /> {i18n.t('buttons.downloadDocument')}</button></div>{copyStatus && <p className="execution-result">{copyStatus}</p>}<pre>{state.incidentCard.markdown || JSON.stringify(state.incidentCard, null, 2)}</pre></article>}
          </section>
        </div>
      </section>
    </main>
  );
}

const rootElement = document.getElementById('root');
if (rootElement) {
  createRoot(rootElement).render(<ConversationalDiagnosisApp />);
}
