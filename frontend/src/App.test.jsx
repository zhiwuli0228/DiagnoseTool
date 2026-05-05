import React from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { act, fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ConversationalDiagnosisApp } from './App.jsx';
import { EvidenceType } from './domain.js';
import { i18n } from './i18n.js';

function createApiClient() {
  return {
    createIncident: vi.fn().mockResolvedValue({ id: 'INC-1', status: 'CREATED' }),
    uploadEvidence: vi.fn((sessionId, payload) => Promise.resolve({
      id: payload.type === EvidenceType.JSTACK ? 'EVD-JSTACK' : 'EVD-LOG',
      type: payload.type,
      source: 'conversation'
    })),
    uploadMetrics: vi.fn().mockResolvedValue({ id: 'MTR-1' }),
    runDiagnosis: vi.fn().mockResolvedValue({
      id: 'RPT-1',
      summary: 'Redis pool exhausted',
      confidence: 'HIGH',
      reportJson: '{"candidateRootCauses":["REDIS_POOL_EXHAUSTED"],"missingInformation":[]}'
    }),
    getDiagnosisProgress: vi.fn().mockResolvedValue({
      status: 'COMPLETED',
      percent: 100,
      step: 'COMPLETED',
      message: 'Diagnosis completed.'
    }),
    generateRecoveryActions: vi.fn().mockResolvedValue([{
      id: 'ACT-1',
      title: 'Check Redis',
      description: 'Inspect Redis latency',
      riskLevel: 'LOW_RISK',
      needApproval: false,
      verification: 'Watch timeout count'
    }]),
    simulateRecoveryAction: vi.fn().mockResolvedValue({
      id: 'ACT-1',
      title: 'Check Redis',
      description: 'Inspect Redis latency',
      riskLevel: 'LOW_RISK',
      needApproval: false,
      verification: 'Watch timeout count',
      executionResult: 'SIMULATED only'
    }),
    generateIncidentCard: vi.fn().mockResolvedValue({ id: 'CARD-1', markdown: '# Redis Review' }),
    createLogAnalysisSession: vi.fn().mockResolvedValue({ id: 'LOG-1', status: 'CREATED' }),
    uploadLogZip: vi.fn().mockResolvedValue({
      id: 'LOG-1',
      status: 'PROCESSED',
      fileSummaries: [{ sourceFile: 'app.log', eventCount: 2, unparsedCount: 1 }]
    }),
    uploadLogDirectory: vi.fn().mockResolvedValue({
      id: 'LOG-1',
      status: 'PROCESSED',
      fileSummaries: [{ sourceFile: 'server.log', eventCount: 3, unparsedCount: 0 }]
    }),
    scanLogDirectory: vi.fn().mockResolvedValue({
      id: 'LOG-2',
      status: 'PROCESSED',
      fileSummaries: [{ sourceFile: 'server.log', eventCount: 3, unparsedCount: 0 }]
    }),
    getLogClusters: vi.fn().mockResolvedValue([{ clusterId: 'CLS-1', exceptionType: 'PaymentException', severity: 'HIGH', count: 2 }]),
    getLogTimeline: vi.fn().mockResolvedValue({ events: [{ evidenceEventId: 'E1', time: '2026-05-05T10:00:00', severity: 'HIGH', summary: 'payment failed' }] }),
    searchLogEvents: vi.fn().mockResolvedValue({
      totalMatched: 2,
      limit: 2,
      events: [{
        id: 'EVT-1',
        timestamp: '2026-05-05T10:00:00',
        level: 'ERROR',
        sourceFile: 'app.log',
        lineNumber: 42,
        traceId: 'trace-1',
        exceptionType: 'PaymentException',
        message: 'Payment failed for order 1001',
        stackTrace: 'at com.geek.PaymentService.charge(PaymentService.java:10)',
        duplicateCount: 3
      }]
    }),
    getEvidencePack: vi.fn().mockResolvedValue({ sessionId: 'LOG-1', incidentSummary: 'Payment failed' }),
    getEvidencePackMarkdown: vi.fn().mockResolvedValue('# Evidence Pack\nPayment failed')
  };
}

const enabledConfig = {
  conversationalDiagnosisEnabled: true,
  requestLimitMs: 0,
  diagnosisProgressPollMs: 1000
};

describe('ConversationalDiagnosisApp', () => {
  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  it('runs the complete diagnosis conversation loop', async () => {
    const user = userEvent.setup();
    const apiClient = createApiClient();

    render(<ConversationalDiagnosisApp apiClient={apiClient} config={enabledConfig} />);

    await user.click(screen.getByRole('button', { name: i18n.t('buttons.startSession') }));
    expect(await screen.findByText('INC-1')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: i18n.t('buttons.submitEvidence') }));
    expect((await screen.findAllByText(new RegExp(i18n.label('evidenceType', EvidenceType.LOG_SNIPPET)))).length).toBeGreaterThan(0);

    fireEvent.change(screen.getByLabelText(i18n.t('labels.redisMetricsJson')), { target: { value: '{"active":10}' } });
    await user.click(screen.getByRole('button', { name: i18n.t('buttons.submitMetrics') }));
    expect(await screen.findByText(i18n.t('conversation.metrics', { count: 1 }))).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: i18n.t('buttons.runDiagnosis') }));
    expect(await screen.findByText('Redis pool exhausted')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: i18n.t('buttons.generateRecovery') }));
    expect(await screen.findByText('Check Redis')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: i18n.t('buttons.simulate') }));
    expect(await screen.findByText('SIMULATED only')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: i18n.t('buttons.generateIncidentCard') }));
    expect(await screen.findByText('# Redis Review')).toBeInTheDocument();
  });

  it('submits log and jstack evidence together from one panel', async () => {
    const user = userEvent.setup();
    const apiClient = createApiClient();

    render(<ConversationalDiagnosisApp apiClient={apiClient} config={enabledConfig} />);

    await user.click(screen.getByRole('button', { name: i18n.t('buttons.startSession') }));
    await screen.findByText('INC-1');
    expect(screen.getByRole('button', { name: i18n.t('labels.logSnippetContent') })).toHaveAttribute('aria-expanded', 'false');
    expect(screen.getByRole('button', { name: i18n.t('labels.jstackContent') })).toHaveAttribute('aria-expanded', 'false');
    await user.click(screen.getByRole('button', { name: i18n.t('labels.jstackContent') }));
    await user.type(screen.getByLabelText(i18n.t('labels.jstackContent')), '"worker-1" RUNNABLE\n at com.geek.Demo.run(Demo.java:1)');
    await user.click(screen.getByRole('button', { name: i18n.t('buttons.submitEvidence') }));

    expect(apiClient.uploadEvidence).toHaveBeenCalledTimes(2);
    expect(apiClient.uploadEvidence).toHaveBeenNthCalledWith(1, 'INC-1', expect.objectContaining({
      type: EvidenceType.LOG_SNIPPET
    }));
    expect(apiClient.uploadEvidence).toHaveBeenNthCalledWith(2, 'INC-1', expect.objectContaining({
      type: EvidenceType.JSTACK
    }));
    expect(await screen.findByText(new RegExp(i18n.label('evidenceType', EvidenceType.JSTACK)))).toBeInTheDocument();
  });

  it('uploads a log zip, searches keywords, and extracts bounded log evidence', async () => {
    const user = userEvent.setup();
    const apiClient = createApiClient();
    const file = new File(['zip-content'], 'logs.zip', { type: 'application/zip' });

    render(<ConversationalDiagnosisApp apiClient={apiClient} config={enabledConfig} />);

    await user.click(screen.getByRole('button', { name: i18n.t('labels.logSnippetContent') }));
    await user.upload(screen.getByLabelText(i18n.t('labels.logZip')), file);
    await user.click(screen.getByRole('button', { name: i18n.t('buttons.uploadLogZip') }));

    expect(apiClient.createLogAnalysisSession).toHaveBeenCalled();
    expect(apiClient.uploadLogZip).toHaveBeenCalledWith('LOG-1', file);
    expect(await screen.findByText(new RegExp(`${i18n.t('labels.logSession')}: LOG-1`))).toBeInTheDocument();
    expect(screen.getByText('app.log')).toBeInTheDocument();
    expect(screen.getByText('PaymentException')).toBeInTheDocument();
    expect(screen.getByText('payment failed')).toBeInTheDocument();
    expect(screen.getByLabelText(i18n.t('labels.logSnippetContent')).value).not.toContain('# Evidence Pack');

    await user.type(screen.getByLabelText(i18n.t('labels.logSearchKeywords')), 'Payment failed');
    await user.selectOptions(screen.getByLabelText(i18n.t('labels.logSearchLevels')), 'ERROR');
    fireEvent.change(screen.getByLabelText(i18n.t('labels.timeFrom')), { target: { value: '2026-05-05T09:00' } });
    fireEvent.change(screen.getByLabelText(i18n.t('labels.timeTo')), { target: { value: '2026-05-05T11:00' } });
    await user.click(screen.getByLabelText(i18n.t('labels.ignoreCase')));
    await user.click(screen.getByLabelText(i18n.t('labels.deduplicateLogs')));
    await user.click(screen.getByRole('button', { name: i18n.t('buttons.searchLogEvents') }));
    expect(apiClient.searchLogEvents).toHaveBeenCalledWith('LOG-1', expect.objectContaining({
      keywords: 'Payment failed',
      levels: ['ERROR'],
      timeFrom: '2026-05-05T09:00',
      timeTo: '2026-05-05T11:00',
      limit: 20,
      includeStackTrace: true,
      ignoreCase: false,
      deduplicate: true
    }));
    expect(await screen.findByText(/Payment failed for order 1001/)).toBeInTheDocument();
    expect(screen.getByText(/x3/)).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: i18n.t('buttons.extractLogSearch') }));
    expect(screen.getByLabelText(i18n.t('labels.logSnippetContent')).value).toContain('Payment failed for order 1001');
    expect(screen.getByLabelText(i18n.t('labels.logSnippetContent')).value).toContain('duplicateCount=3');
  });

  it('submits a log directory scan and waits for keyword extraction before filling snippet', async () => {
    const user = userEvent.setup();
    const apiClient = createApiClient();
    const file = new File(['2026-05-05 ERROR payment failed'], 'server.log', { type: 'text/plain' });
    Object.defineProperty(file, 'webkitRelativePath', {
      configurable: true,
      value: 'incident-001/server.log'
    });

    render(<ConversationalDiagnosisApp apiClient={apiClient} config={enabledConfig} />);

    await user.click(screen.getByRole('button', { name: i18n.t('labels.logSnippetContent') }));
    await user.click(screen.getByLabelText(i18n.t('labels.logSourceDirectory')));
    await user.upload(screen.getByLabelText(i18n.t('labels.logDirectory')), file);
    await user.click(screen.getByRole('button', { name: i18n.t('buttons.scanLogDirectory') }));
    expect(apiClient.uploadLogDirectory).toHaveBeenCalledWith('LOG-1', [file]);
    expect(await screen.findByText('server.log')).toBeInTheDocument();
    expect(screen.getByLabelText(i18n.t('labels.logSnippetContent')).value).not.toContain('Payment failed');
  });

  it('keeps large log summaries readable after zip parsing', async () => {
    const user = userEvent.setup();
    const apiClient = createApiClient();
    apiClient.uploadLogZip.mockResolvedValue({
      id: 'LOG-1',
      status: 'PROCESSED',
      fileSummaries: Array.from({ length: 12 }, (_, index) => ({
        sourceFile: `app-${index}.log`,
        eventCount: index + 1,
        unparsedCount: 0
      }))
    });
    apiClient.getLogClusters.mockResolvedValue([{
      clusterId: 'CLS-559684b3',
      sampleLogs: ['payment failed traceId=abc-123'],
      severity: 'HIGH',
      count: 9
    }]);
    apiClient.getLogTimeline.mockResolvedValue({
      events: [{
        evidenceEventId: 'E1',
        time: '2026-05-05T10:00:00',
        severity: 'MEDIUM',
        summary: 'x'.repeat(300),
        sourceFile: 'app.log',
        threadName: 'worker-1'
      }]
    });
    const file = new File(['zip-content'], 'logs.zip', { type: 'application/zip' });

    render(<ConversationalDiagnosisApp apiClient={apiClient} config={enabledConfig} />);

    await user.click(screen.getByRole('button', { name: i18n.t('labels.logSnippetContent') }));
    await user.upload(screen.getByLabelText(i18n.t('labels.logZip')), file);
    await user.click(screen.getByRole('button', { name: i18n.t('buttons.uploadLogZip') }));

    expect(await screen.findByText(i18n.t('labels.logFileSummary', { shown: 8, total: 12 }))).toBeInTheDocument();
    expect(screen.queryByText('app-11.log')).not.toBeInTheDocument();
    expect(screen.getByText('payment failed traceId=abc-123')).toBeInTheDocument();
    expect(screen.queryByText('CLS-559684b3')).not.toBeInTheDocument();
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
  });

  it('shows default Chinese text and key control tips', async () => {
    const user = userEvent.setup();
    const apiClient = createApiClient();
    const file = new File(['zip-content'], 'logs.zip', { type: 'application/zip' });
    render(<ConversationalDiagnosisApp apiClient={apiClient} config={enabledConfig} />);

    expect(screen.getByText(i18n.t('app.subtitle'))).toBeInTheDocument();
    expect(screen.getByLabelText(i18n.t('labels.title'))).toHaveAttribute('title', i18n.t('tips.title'));
    expect(screen.getByRole('button', { name: i18n.t('labels.logSnippetContent') })).toHaveAttribute('title', i18n.t('tips.logSnippetContent'));
    expect(screen.getByRole('button', { name: i18n.t('labels.jstackContent') })).toHaveAttribute('title', i18n.t('tips.jstackContent'));
    await user.click(screen.getByRole('button', { name: i18n.t('labels.logSnippetContent') }));
    expect(screen.getByLabelText(i18n.t('labels.logZip'))).toHaveAttribute('title', i18n.t('tips.logZip'));
    await user.upload(screen.getByLabelText(i18n.t('labels.logZip')), file);
    await user.click(screen.getByRole('button', { name: i18n.t('buttons.uploadLogZip') }));
    expect(await screen.findByText(new RegExp(`${i18n.t('labels.logSession')}: LOG-1`))).toBeInTheDocument();
    expect(screen.getByLabelText(i18n.t('labels.logSearchKeywords'))).toHaveAttribute('title', i18n.t('tips.logSearchKeywords'));
    expect(screen.getByText(i18n.t('tips.logSearchKeywords'))).toBeInTheDocument();
    expect(screen.getByText(i18n.t('tips.logSearchLevels'))).toBeInTheDocument();
    expect(screen.getByText(i18n.t('tips.logSearchLimit'))).toBeInTheDocument();
    expect(screen.getByText(i18n.t('tips.searchLogEvents'))).toBeInTheDocument();
    expect(screen.getByLabelText(i18n.t('labels.timeFrom'))).toHaveAttribute('title', i18n.t('tips.timeFrom'));
    expect(screen.getByLabelText(i18n.t('labels.ignoreCase'))).toBeInTheDocument();
    expect(screen.getByLabelText(i18n.t('labels.deduplicateLogs'))).toBeInTheDocument();
    expect(screen.getByRole('button', { name: i18n.t('buttons.runDiagnosis') })).toHaveAttribute('title', i18n.t('tips.runDiagnosis'));
  });

  it('shows and exports the current result document', async () => {
    const user = userEvent.setup();
    const apiClient = createApiClient();
    const writeText = vi.fn().mockResolvedValue();
    Object.defineProperty(navigator, 'clipboard', {
      configurable: true,
      value: { writeText }
    });
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: vi.fn().mockReturnValue('blob:result')
    });
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: vi.fn()
    });
    const originalCreateElement = document.createElement.bind(document);
    const createElement = vi.spyOn(document, 'createElement').mockImplementation((tagName) => {
      if (tagName === 'a') {
        return { href: '', download: '', click: vi.fn() };
      }
      return originalCreateElement(tagName);
    });

    render(<ConversationalDiagnosisApp apiClient={apiClient} config={enabledConfig} />);

    await user.click(screen.getByRole('button', { name: i18n.t('buttons.startSession') }));
    await screen.findByText('INC-1');
    await user.click(screen.getByRole('button', { name: i18n.t('buttons.runDiagnosis') }));
    await screen.findByText('Redis pool exhausted');
    await user.click(screen.getByRole('button', { name: i18n.t('buttons.generateRecovery') }));
    await screen.findByText('Check Redis');
    await user.click(screen.getByRole('button', { name: i18n.t('buttons.generateIncidentCard') }));
    await screen.findByText('# Redis Review');

    await user.click(screen.getAllByRole('button', { name: i18n.t('buttons.copyDocument') })[0]);
    expect(writeText).toHaveBeenCalledWith('# Redis Review');
    expect(await screen.findByText(i18n.t('conversation.copied'))).toBeInTheDocument();

    await user.click(screen.getAllByRole('button', { name: i18n.t('buttons.downloadDocument') })[0]);
    expect(URL.createObjectURL).toHaveBeenCalled();
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:result');
    createElement.mockRestore();
  });

  it('polls and renders diagnosis progress while diagnosis is running', async () => {
    let resolveDiagnosis;
    const apiClient = createApiClient();
    apiClient.runDiagnosis.mockReturnValue(new Promise((resolve) => {
      resolveDiagnosis = resolve;
    }));
    apiClient.getDiagnosisProgress
      .mockResolvedValueOnce({ status: 'RUNNING', percent: 25, step: 'BUILDING_CONTEXT', message: 'Building diagnosis context.' })
      .mockResolvedValueOnce({ status: 'RUNNING', percent: 70, step: 'GENERATING_REPORT', message: 'Generating diagnosis report.' });

    render(<ConversationalDiagnosisApp apiClient={apiClient} config={{ ...enabledConfig, diagnosisProgressPollMs: 1000 }} />);

    fireEvent.click(screen.getByRole('button', { name: i18n.t('buttons.startSession') }));
    await screen.findByText('INC-1');
    vi.useFakeTimers();
    fireEvent.click(screen.getByRole('button', { name: i18n.t('buttons.runDiagnosis') }));
    await act(async () => {});

    expect(screen.getByRole('progressbar', { name: i18n.t('progress.title') })).toHaveAttribute('aria-valuenow', '25');
    expect(screen.getByText(i18n.t('progress.flow.analyzeEvidence'))).toBeInTheDocument();
    expect(screen.getByText(i18n.t('progress.flow.requestModel'))).toBeInTheDocument();
    expect(screen.getByText(i18n.t('progress.flow.analyzeResult'))).toBeInTheDocument();
    expect(screen.getAllByText(new RegExp(i18n.progressStep('BUILDING_CONTEXT'))).length).toBeGreaterThan(0);

    await act(async () => {
      vi.advanceTimersByTime(1000);
    });

    expect(screen.getAllByText(new RegExp(i18n.progressStep('GENERATING_REPORT'))).length).toBeGreaterThan(0);
    expect(screen.getByRole('progressbar', { name: i18n.t('progress.title') })).toHaveAttribute('aria-valuenow', '70');

    await act(async () => {
      resolveDiagnosis({ id: 'RPT-1', summary: 'Redis pool exhausted', confidence: 'HIGH', reportJson: '{}' });
    });
    expect(screen.getByText('Redis pool exhausted')).toBeInTheDocument();
  });
});
