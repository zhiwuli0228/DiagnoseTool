package com.geek.threaddoctor.pattern;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
// 统一调度所有故障模式检测器，只把命中的模式交给诊断报告层。
public class FaultPatternDetectionService {
    private final List<FaultPatternDetector> detectors;

    public FaultPatternDetectionService(List<FaultPatternDetector> detectors) {
        this.detectors = detectors;
    }

    public List<DetectionResult> detect(DiagnosisContext context) {
        return detectors.stream().map(detector -> detector.detect(context)).filter(DetectionResult::matched).toList();
    }
}
