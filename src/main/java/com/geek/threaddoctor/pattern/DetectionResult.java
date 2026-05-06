/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.pattern;

import com.geek.threaddoctor.common.ConfidenceLevel;
import java.util.List;

/**
 * 承载不可变业务数据。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public record DetectionResult(
        FaultPatternType type,
        boolean matched,
        ConfidenceLevel confidence,
        String summary,
        List<String> supportingEvidenceIds) {
    /**
     * 创建未命中的检测结果。
     *
     * @param type 类型
     * @return 未命中的检测结果
     */
    public static DetectionResult noMatch(FaultPatternType type) {
        return new DetectionResult(type, false, ConfidenceLevel.LOW, "No matching evidence", List.of());
    }
}
