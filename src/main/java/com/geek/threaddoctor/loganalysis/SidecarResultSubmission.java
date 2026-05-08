/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.util.List;
import java.util.Map;

/**
 * 提交到后端的 Sidecar 结构化结果。
 *
 * @author zhiwuli
 * @since 2026-05-08
 */
public record SidecarResultSubmission(
        List<LogSource> sources,
        List<LogFileSummary> fileSummaries,
        List<LogEvent> selectedEvents,
        EvidencePack evidencePack,
        String evidencePackMarkdown,
        Map<String, String> metadata) {
}
