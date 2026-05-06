import { DiagnosisProgressStatus, DiagnosisProgressStep, EvidenceType, SeverityLevel, WorkflowStage } from './domain.js';

export const defaultLocale = 'zh-CN';

export const messages = {
  'zh-CN': {
    app: {
      title: 'Thread Doctor',
      subtitle: '面向 Java 生产问题的对话式诊断工作台',
      disabled: '诊断入口当前已关闭，请联系管理员开启配置。',
      conversation: '诊断对话',
      incident: '问题信息',
      evidence: '证据采集',
      logAnalysis: '日志智能解析',
      diagnosis: '问题诊断',
      recovery: '恢复建议',
      report: '结果文档',
      incidentCard: '事件卡片'
    },
    labels: {
      title: '标题',
      description: '描述',
      severity: '严重级别',
      evidenceType: '证据类型',
      evidenceContent: '证据内容',
      logSnippetContent: '日志片段',
      jstackContent: 'jstack / 堆栈片段',
      confidence: '置信度',
      risk: '风险',
      approval: '需要审批',
      verification: '验证方式',
      jvmMetricsJson: 'JVM 指标 JSON',
      redisMetricsJson: 'Redis 指标 JSON',
      kafkaMetricsJson: 'Kafka 指标 JSON',
      dbMetricsJson: 'DB 指标 JSON',
      logZip: '日志 ZIP 包',
      logDirectory: '日志目录',
      logSourceZip: '使用 ZIP 包',
      logSourceDirectory: '使用本地目录',
      logSession: '日志会话',
      logFiles: '日志文件',
      logClusters: '异常聚类',
      logTimeline: '日志时间线',
      evidencePack: '证据包',
      generatedDocument: '生成文档',
      logSearchKeywords: '检索关键字',
      logSearchLevels: '日志级别',
      allLogLevels: '全部级别',
      logSearchLimit: '提取条数',
      includeStackTrace: '包含堆栈',
      ignoreCase: '忽略大小写',
      deduplicateLogs: '去重压缩',
      timeFrom: '开始时间',
      timeTo: '结束时间',
      logSearchResults: '检索结果',
      logSearchMatched: '命中 {count} 条，当前最多展示 {limit} 条',
      logFileSummary: '显示 {shown} / {total} 个文件，更多文件已折叠'
    },
    buttons: {
      startSession: '开始诊断',
      submitEvidence: '提交证据',
      submitMetrics: '提交指标',
      uploadLogZip: '解析 ZIP',
      scanLogDirectory: '解析目录',
      searchLogEvents: '检索关键日志',
      extractLogSearch: '填入日志片段',
      generateCodexTask: '生成 Codex 任务',
      generateOpenSpecDraft: '生成 OpenSpec 草稿',
      runDiagnosis: '开始诊断分析',
      generateRecovery: '生成恢复建议',
      simulate: '模拟执行',
      generateIncidentCard: '生成事件卡片',
      copyDocument: '复制文档',
      downloadDocument: '下载文档'
    },
    tips: {
      title: '填写能概括本次问题的简短标题。',
      description: '描述故障现象、影响范围、开始时间和已经观察到的线索。',
      severity: '选择本次问题对业务影响的严重程度。',
      evidenceType: '选择要提交给诊断流程的证据类型。',
      evidenceContent: '提交日志、堆栈或其他能支撑诊断的原始证据。',
      logSnippetContent: '粘贴关键日志片段，或先解析日志包和目录后再提取关键内容。',
      jstackContent: '粘贴 jstack 或线程堆栈片段，便于分析阻塞、死锁和线程池问题。',
      metricsJson: '填写 JVM、Redis、Kafka 或 DB 的 JSON 指标快照。',
      logZip: '选择本地日志 ZIP 包；ZIP 包和日志目录只能二选一。',
      logDirectory: '选择本地日志目录，目录内层级会按 ZIP 包方式上传解析；ZIP 包和日志目录只能二选一。',
      uploadLogZip: '上传并解析 ZIP 包，但不会自动写入诊断证据，需要先检索关键日志。',
      scanLogDirectory: '上传并解析选择的本地目录，但不会自动写入诊断证据，需要先检索关键日志。',
      logSearchRequired: '请先解析 ZIP 包或日志目录，再输入关键字检索需要提交的日志片段。',
      logSearchKeywords: '支持多个关键字片段，每行填写一个片段；片段内部的空格、逗号、分号和中文标点会按原文匹配。',
      logSearchLevels: '可选择 TRACE、DEBUG、INFO、WARN、ERROR 中任意级别；不选择表示不限级别。',
      logSearchLimit: '限制提取到前台和诊断证据中的日志事件数量，避免大日志直接进入大模型。',
      includeStackTrace: '开启后检索结果会带上堆栈正文，关闭后只返回事件摘要。',
      ignoreCase: '开启后关键字和字段匹配不区分大小写。',
      deduplicateLogs: '开启后相同日志会压缩为一条，并在结果中显示重复次数。',
      timeFrom: '可选，限制只检索该时间之后的日志事件。',
      timeTo: '可选，限制只检索该时间之前的日志事件。',
      searchLogEvents: '按照关键字、级别、时间范围和大小写设置检索关键日志，不会直接提交诊断。',
      extractLogSearch: '把当前检索结果整理为日志片段，之后通过提交证据进入诊断流程。',
      generateCodexTask: '根据证据包生成可下载的 Codex 调查任务 Markdown。',
      generateOpenSpecDraft: '根据证据包生成可下载的 OpenSpec 草稿，不会自动写入仓库。',
      startSession: '创建本次问题诊断会话。',
      submitEvidence: '提交日志片段和 jstack 片段，可一次提交其中一个或两个。',
      submitMetrics: '提交各类指标快照作为诊断上下文。',
      runDiagnosis: '基于已提交证据请求后端诊断能力并生成分析报告。',
      generateRecovery: '根据诊断报告生成恢复建议。',
      simulate: '仅模拟恢复动作，便于评估风险。',
      generateIncidentCard: '生成适合复盘和流转的 Markdown 事件卡片。',
      copyDocument: '复制当前 Markdown 文档内容。',
      downloadDocument: '下载当前 Markdown 文档。'
    },
    conversation: {
      welcome: '请先开始问题会话，然后提交证据。日志包或目录解析后，需要先检索关键日志，再填入日志片段并提交。',
      session: '问题会话 {id} 已创建，当前状态：{status}',
      evidence: '已提交 {type} 证据，来源：{source}',
      metrics: '已提交 {count} 组指标快照',
      report: '诊断报告已生成：{summary}',
      reportFallback: '诊断报告',
      recovery: '已生成 {count} 条恢复建议',
      card: '事件卡片已生成，可复制或下载。',
      duplicateTask: '相同任务正在执行，请等待当前操作完成。',
      rateLimited: '操作过于频繁，请稍后再试。',
      requestFailed: '请求失败，请检查后端服务和日志。',
      copied: '文档已复制。'
    },
    progress: {
      title: '诊断进度',
      flowLabel: '诊断流程',
      current: '当前步骤：{step}',
      warning: '进度刷新失败，诊断任务可能仍在后端执行。',
      percent: '{percent}%',
      flow: {
        prepare: '准备诊断',
        analyzeEvidence: '分析证据',
        requestModel: '请求大模型',
        analyzeResult: '分析结果',
        completed: '完成'
      },
      messages: {
        started: '诊断已开始。'
      },
      statuses: {
        [DiagnosisProgressStatus.NOT_STARTED]: '未开始',
        [DiagnosisProgressStatus.RUNNING]: '进行中',
        [DiagnosisProgressStatus.COMPLETED]: '已完成',
        [DiagnosisProgressStatus.FAILED]: '失败'
      },
      steps: {
        [DiagnosisProgressStep.PENDING]: '等待开始',
        [DiagnosisProgressStep.STARTED]: '启动诊断',
        [DiagnosisProgressStep.BUILDING_CONTEXT]: '分析证据',
        [DiagnosisProgressStep.DETECTING_PATTERNS]: '识别问题模式',
        [DiagnosisProgressStep.GENERATING_REPORT]: '请求大模型',
        [DiagnosisProgressStep.VALIDATING_REPORT]: '分析结果',
        [DiagnosisProgressStep.PERSISTING_REPORT]: '生成结果文档',
        [DiagnosisProgressStep.COMPLETED]: '诊断完成',
        [DiagnosisProgressStep.FAILED]: '诊断失败'
      }
    },
    enums: {
      severity: {
        [SeverityLevel.LOW]: '低',
        [SeverityLevel.MEDIUM]: '中',
        [SeverityLevel.HIGH]: '高',
        [SeverityLevel.CRITICAL]: '严重'
      },
      evidenceType: {
        [EvidenceType.LOG_SNIPPET]: '日志片段',
        [EvidenceType.JSTACK]: 'jstack 片段'
      },
      stage: {
        [WorkflowStage.IDLE]: '未开始',
        [WorkflowStage.COLLECTING_EVIDENCE]: '采集证据',
        [WorkflowStage.DIAGNOSING]: '诊断中',
        [WorkflowStage.REVIEWING_REPORT]: '查看报告',
        [WorkflowStage.RECOMMENDING_RECOVERY]: '恢复建议',
        [WorkflowStage.COMPLETED]: '已完成',
        [WorkflowStage.FAILED]: '失败'
      },
      confidence: {
        LOW: '低',
        MEDIUM: '中',
        MEDIUM_HIGH: '中高',
        HIGH: '高'
      },
      risk: {
        LOW_RISK: '低风险',
        MEDIUM_RISK: '中风险',
        HIGH_RISK: '高风险'
      }
    }
  }
};

Object.assign(messages['zh-CN'].labels, {
  localizationStatus: 'Localization status',
  unresolvedDiagnosis: 'Unresolved diagnosis',
  unresolvedReason: 'Reason'
});

Object.assign(messages['zh-CN'].buttons, {
  continueEvidence: 'Provide more information',
  copyCodebasePrompt: 'Copy codebase prompt'
});

Object.assign(messages['zh-CN'].tips, {
  continueEvidence: 'Open evidence inputs so more key information can be submitted.',
  copyCodebasePrompt: 'Copy the generated prompt for Codex or OpenCode. No tool is executed automatically.'
});

messages['zh-CN'].enums.localizationStatus = {
  LOCALIZED: 'Localized',
  UNRESOLVED: 'Unresolved',
  NEEDS_MORE_EVIDENCE: 'Needs more evidence'
};

function resolvePath(resource, key) {
  return key.split('.').reduce((current, part) => current?.[part], resource);
}

function interpolate(template, params) {
  return Object.entries(params).reduce(
    (text, [key, value]) => text.replaceAll(`{${key}}`, String(value ?? '')),
    template
  );
}

export function createI18n(locale = defaultLocale, resources = messages) {
  const resource = resources[locale] || resources[defaultLocale];
  return {
    locale,
    t(key, params = {}) {
      const value = resolvePath(resource, key);
      return typeof value === 'string' ? interpolate(value, params) : key;
    },
    label(group, value) {
      return resource.enums?.[group]?.[value] || value || '';
    },
    progressStatus(value) {
      return resource.progress?.statuses?.[value] || value || '';
    },
    progressStep(value) {
      return resource.progress?.steps?.[value] || value || '';
    }
  };
}

export const i18n = createI18n();
