/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.api;

import com.geek.threaddoctor.diagnosis.DiagnosisProgress;
import com.geek.threaddoctor.diagnosis.DiagnosisProgressService;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.diagnosis.DiagnosisReportService;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

/**
 * 提供业务说明。
 *
 * @author zhiwuli
 * @since 2026-05-07
 */
@RestController
@RequestMapping("/api/incidents/{sessionId}")
@Validated
public class DiagnosisController {
    private final DiagnosisReportService reportService;
    private final DiagnosisProgressService progressService;

    /**
     * 执行业务操作。
     *
     * @param reportService 业务服务依赖
     * @param progressService 业务服务依赖
     */
    public DiagnosisController(DiagnosisReportService reportService, DiagnosisProgressService progressService) {
        this.reportService = reportService;
        this.progressService = progressService;
    }

    @PostMapping("/diagnose")
    DiagnosisReport diagnose(@PathVariable @Pattern(regexp = "INC-[A-Za-z0-9-]{1,80}") String sessionId) {
        return reportService.diagnose(sessionId);
    }

    @GetMapping("/report")
    DiagnosisReport latest(@PathVariable @Pattern(regexp = "INC-[A-Za-z0-9-]{1,80}") String sessionId) {
        return reportService.latest(sessionId);
    }

    @GetMapping("/diagnosis-progress")
    DiagnosisProgress progress(@PathVariable @Pattern(regexp = "INC-[A-Za-z0-9-]{1,80}") String sessionId) {
        return progressService.current(sessionId);
    }
}
