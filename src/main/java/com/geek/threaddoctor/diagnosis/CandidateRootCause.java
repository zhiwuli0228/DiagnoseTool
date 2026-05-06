/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.diagnosis;

import java.util.List;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record CandidateRootCause(String causeId, String title, String description, double score, List<String> supportingEvidenceIds, List<String> riskNotes) {
}
