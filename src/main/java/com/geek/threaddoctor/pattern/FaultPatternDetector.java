/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.pattern;

/**
 * 定义组件对外契约。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public interface FaultPatternDetector {
    FaultPatternType type();

    DetectionResult detect(DiagnosisContext context);
}
