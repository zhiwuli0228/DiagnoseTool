import { describe, expect, it, vi } from 'vitest';
import { createDiagnosisApi } from './diagnosisApi.js';

function okJson(body) {
  return {
    ok: true,
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(typeof body === 'string' ? body : JSON.stringify(body))
  };
}

describe('diagnosisApi', () => {
  it('maps incident creation payload to backend path', async () => {
    const fetchImpl = vi.fn().mockResolvedValue(okJson({ id: 'INC-1' }));
    const api = createDiagnosisApi(fetchImpl);

    await api.createIncident({ title: 'Timeout', description: 'Redis timeout', severity: 'HIGH' });

    expect(fetchImpl).toHaveBeenCalledWith('/api/incidents', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ title: 'Timeout', description: 'Redis timeout', severity: 'HIGH' })
    }));
  });

  it('maps diagnosis and closure endpoints', async () => {
    const fetchImpl = vi.fn().mockResolvedValue(okJson({ id: 'OK' }));
    const api = createDiagnosisApi(fetchImpl);

    await api.uploadEvidence('INC-1', { type: 'LOG_SNIPPET', source: 'conversation', content: 'log' });
    await api.uploadMetrics('INC-1', { redisMetricsJson: '{}' });
    await api.runDiagnosis('INC-1');
    await api.getDiagnosisProgress('INC-1');
    await api.generateRecoveryActions('INC-1');
    await api.simulateRecoveryAction('INC-1', 'ACT-1');
    await api.generateIncidentCard('INC-1');

    expect(fetchImpl.mock.calls.map((call) => call[0])).toEqual([
      '/api/incidents/INC-1/evidences',
      '/api/incidents/INC-1/metrics',
      '/api/incidents/INC-1/diagnose',
      '/api/incidents/INC-1/diagnosis-progress',
      '/api/incidents/INC-1/recovery-actions',
      '/api/incidents/INC-1/recovery-actions/ACT-1/execute',
      '/api/incidents/INC-1/incident-card'
    ]);
  });

  it('throws backend error text when request fails', async () => {
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      text: () => Promise.resolve('bad request')
    });
    const api = createDiagnosisApi(fetchImpl);

    await expect(api.runDiagnosis('INC-1')).rejects.toThrow('bad request');
  });

  it('uses sanitized backend validation message when request fails with json error', async () => {
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      text: () => Promise.resolve(JSON.stringify({ message: 'apiKey=[SECRET]' }))
    });
    const api = createDiagnosisApi(fetchImpl);

    await expect(api.runDiagnosis('INC-1')).rejects.toThrow('apiKey=[SECRET]');
  });

  it('maps log analysis endpoints and zip upload form data', async () => {
    const fetchImpl = vi.fn().mockResolvedValue(okJson({ id: 'LOG-1' }));
    const api = createDiagnosisApi(fetchImpl);
    const file = new File(['zip'], 'logs.zip', { type: 'application/zip' });

    await api.createLogAnalysisSession();
    await api.uploadLogZip('LOG-1', file);
    await api.uploadLogDirectory('LOG-1', [file]);
    await api.scanLogDirectory('LOG-1', 'E:\\logs');
    await api.submitSidecarResult('LOG-1', { selectedEvents: [] });
    await api.searchLogEvents('LOG-1', { keywords: 'timeout,failed', levels: ['ERROR'], ignoreCase: true, timeFrom: '2026-05-05T10:00', limit: 10 });
    await api.getLogClusters('LOG-1');
    await api.getLogTimeline('LOG-1');
    await api.getEvidencePack('LOG-1');
    await api.generateCodexTask('LOG-1');
    await api.generateOpenSpecDraft('LOG-1');

    expect(fetchImpl.mock.calls.map((call) => call[0])).toEqual([
      '/api/log-analysis/sessions',
      '/api/log-analysis/sessions/LOG-1/zip',
      '/api/log-analysis/sessions/LOG-1/directory',
      '/api/log-analysis/sessions/LOG-1/directory-scan',
      '/api/log-analysis/sessions/LOG-1/sidecar-result',
      '/api/log-analysis/sessions/LOG-1/search',
      '/api/log-analysis/sessions/LOG-1/clusters',
      '/api/log-analysis/sessions/LOG-1/timeline',
      '/api/log-analysis/sessions/LOG-1/evidence-pack',
      '/api/log-analysis/sessions/LOG-1/codex-task',
      '/api/log-analysis/sessions/LOG-1/openspec-change-draft'
    ]);
    expect(fetchImpl.mock.calls[1][1].body).toBeInstanceOf(FormData);
    expect(fetchImpl.mock.calls[1][1].headers).not.toHaveProperty('Content-Type');
    expect(fetchImpl.mock.calls[2][1].body).toBeInstanceOf(FormData);
    expect(fetchImpl.mock.calls[2][1].headers).not.toHaveProperty('Content-Type');
  });

  it('maps sidecar endpoints to loopback service', async () => {
    const fetchImpl = vi.fn().mockResolvedValue(okJson({ status: 'UP' }));
    const api = createDiagnosisApi(fetchImpl, 'http://127.0.0.1:18765/api/sidecar');

    await api.getSidecarHealth();
    await api.analyzeSidecarZip('E:\\logs\\incident.zip');
    await api.analyzeSidecarDirectory('E:\\logs\\incident');
    await api.searchSidecarEvents('LOCAL-1', { keywords: 'timeout' });
    await api.getSidecarSnapshot('LOCAL-1');

    expect(fetchImpl.mock.calls.map((call) => [call[0], call[1].method])).toEqual([
      ['http://127.0.0.1:18765/api/sidecar/health', 'GET'],
      ['http://127.0.0.1:18765/api/sidecar/analysis/zip', 'POST'],
      ['http://127.0.0.1:18765/api/sidecar/analysis/directory', 'POST'],
      ['http://127.0.0.1:18765/api/sidecar/sessions/LOCAL-1/search', 'POST'],
      ['http://127.0.0.1:18765/api/sidecar/sessions/LOCAL-1/snapshot', 'GET']
    ]);
  });

  it('loads evidence pack markdown as text', async () => {
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: true,
      text: () => Promise.resolve('# Evidence Pack')
    });
    const api = createDiagnosisApi(fetchImpl);

    await expect(api.getEvidencePackMarkdown('LOG-1')).resolves.toBe('# Evidence Pack');
    expect(fetchImpl).toHaveBeenCalledWith('/api/log-analysis/sessions/LOG-1/evidence-pack?format=markdown', expect.objectContaining({
      method: 'GET'
    }));
  });

  it('maps llm configuration endpoints', async () => {
    const fetchImpl = vi.fn().mockResolvedValue(okJson({ activeSource: 'frontend' }));
    const api = createDiagnosisApi(fetchImpl);

    await api.getLlmConfiguration();
    await api.saveLlmConfiguration({ baseUrl: 'https://llm.test/v1', model: 'qwen' });
    await api.clearLlmConfiguration();

    expect(fetchImpl.mock.calls.map((call) => [call[0], call[1].method])).toEqual([
      ['/api/llm/configuration', 'GET'],
      ['/api/llm/configuration', 'PUT'],
      ['/api/llm/configuration', 'DELETE']
    ]);
    expect(fetchImpl.mock.calls[1][1].body).toBe(JSON.stringify({
      baseUrl: 'https://llm.test/v1',
      model: 'qwen'
    }));
  });
});
