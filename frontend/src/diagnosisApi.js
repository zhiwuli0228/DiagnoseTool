const incidentsBase = '/api/incidents';
const logAnalysisBase = '/api/log-analysis/sessions';

async function requestJson(path, options = {}, fetchImpl = fetch) {
  const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;
  const response = await fetchImpl(path, {
    ...options,
    headers: {
      ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
      ...(options.headers || {})
    }
  });
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed: ${response.status}`);
  }
  return response.json();
}

async function requestText(path, options = {}, fetchImpl = fetch) {
  const response = await fetchImpl(path, options);
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed: ${response.status}`);
  }
  return response.text();
}

export function createDiagnosisApi(fetchImpl = fetch) {
  return {
    createIncident(payload) {
      return requestJson(incidentsBase, {
        method: 'POST',
        body: JSON.stringify(payload)
      }, fetchImpl);
    },
    uploadEvidence(sessionId, payload) {
      return requestJson(`${incidentsBase}/${sessionId}/evidences`, {
        method: 'POST',
        body: JSON.stringify(payload)
      }, fetchImpl);
    },
    uploadMetrics(sessionId, payload) {
      return requestJson(`${incidentsBase}/${sessionId}/metrics`, {
        method: 'POST',
        body: JSON.stringify(payload)
      }, fetchImpl);
    },
    runDiagnosis(sessionId) {
      return requestJson(`${incidentsBase}/${sessionId}/diagnose`, {
        method: 'POST'
      }, fetchImpl);
    },
    getDiagnosisProgress(sessionId) {
      return requestJson(`${incidentsBase}/${sessionId}/diagnosis-progress`, {
        method: 'GET'
      }, fetchImpl);
    },
    generateRecoveryActions(sessionId) {
      return requestJson(`${incidentsBase}/${sessionId}/recovery-actions`, {
        method: 'POST'
      }, fetchImpl);
    },
    simulateRecoveryAction(sessionId, actionId) {
      return requestJson(`${incidentsBase}/${sessionId}/recovery-actions/${actionId}/execute`, {
        method: 'POST'
      }, fetchImpl);
    },
    generateIncidentCard(sessionId) {
      return requestJson(`${incidentsBase}/${sessionId}/incident-card`, {
        method: 'POST'
      }, fetchImpl);
    },
    createLogAnalysisSession() {
      return requestJson(logAnalysisBase, {
        method: 'POST'
      }, fetchImpl);
    },
    uploadLogZip(sessionId, file) {
      const formData = new FormData();
      formData.append('file', file);
      return requestJson(`${logAnalysisBase}/${sessionId}/zip`, {
        method: 'POST',
        headers: {},
        body: formData
      }, fetchImpl);
    },
    uploadLogDirectory(sessionId, files) {
      const formData = new FormData();
      Array.from(files || []).forEach((file) => {
        formData.append('files', file, file.webkitRelativePath || file.name);
      });
      return requestJson(`${logAnalysisBase}/${sessionId}/directory`, {
        method: 'POST',
        headers: {},
        body: formData
      }, fetchImpl);
    },
    scanLogDirectory(sessionId, path) {
      return requestJson(`${logAnalysisBase}/${sessionId}/directory-scan`, {
        method: 'POST',
        body: JSON.stringify({ path })
      }, fetchImpl);
    },
    searchLogEvents(sessionId, payload) {
      return requestJson(`${logAnalysisBase}/${sessionId}/search`, {
        method: 'POST',
        body: JSON.stringify(payload)
      }, fetchImpl);
    },
    getLogClusters(sessionId) {
      return requestJson(`${logAnalysisBase}/${sessionId}/clusters`, {
        method: 'GET'
      }, fetchImpl);
    },
    getLogTimeline(sessionId) {
      return requestJson(`${logAnalysisBase}/${sessionId}/timeline`, {
        method: 'GET'
      }, fetchImpl);
    },
    getEvidencePack(sessionId) {
      return requestJson(`${logAnalysisBase}/${sessionId}/evidence-pack`, {
        method: 'GET'
      }, fetchImpl);
    },
    getEvidencePackMarkdown(sessionId) {
      return requestText(`${logAnalysisBase}/${sessionId}/evidence-pack?format=markdown`, {
        method: 'GET'
      }, fetchImpl);
    },
    generateCodexTask(sessionId) {
      return requestJson(`${logAnalysisBase}/${sessionId}/codex-task`, {
        method: 'POST'
      }, fetchImpl);
    },
    generateOpenSpecDraft(sessionId) {
      return requestJson(`${logAnalysisBase}/${sessionId}/openspec-change-draft`, {
        method: 'POST'
      }, fetchImpl);
    }
  };
}

export const diagnosisApi = createDiagnosisApi();
