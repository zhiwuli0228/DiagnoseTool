/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.util.List;
import java.util.Map;

/**
 * Sidecar 本地分析快照。
 *
 * @author zhiwuli
 * @since 2026-05-08
 */
public record SidecarAnalysisSnapshot(
        LogAnalysisSession session,
        List<LogCluster> clusters,
        IncidentTimeline timeline,
        EvidencePack evidencePack,
        String evidencePackMarkdown,
        LogSearchResult selectedEvents,
        Map<String, String> metadata) {
}
