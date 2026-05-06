/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.pattern;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
// 统一调度所有故障模式检测器，只把命中的模式交给诊断报告层。
/**
 * 封装业务逻辑和数据处理能力。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
public class FaultPatternDetectionService {
    private final List<FaultPatternDetector> detectors;

    /**
     * 执行业务操作。
     *
     * @param detectors 业务参数
     */
    public FaultPatternDetectionService(List<FaultPatternDetector> detectors) {
        this.detectors = detectors;
    }

    /**
     * 执行故障模式检测。
     *
     * @param context 诊断上下文
     * @return 检测结果
     */
    public List<DetectionResult> detect(DiagnosisContext context) {
        return detectors.stream().map(detector -> detector.detect(context)).filter(DetectionResult::matched).toList();
    }
}
