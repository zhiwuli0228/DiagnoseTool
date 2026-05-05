package com.geek.threaddoctor.api;

import com.geek.threaddoctor.diagnosis.DiagnosisProgress;
import com.geek.threaddoctor.diagnosis.DiagnosisProgressService;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.diagnosis.DiagnosisReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents/{sessionId}")
public class DiagnosisController {
    private final DiagnosisReportService reportService;
    private final DiagnosisProgressService progressService;

    public DiagnosisController(DiagnosisReportService reportService, DiagnosisProgressService progressService) {
        this.reportService = reportService;
        this.progressService = progressService;
    }

    @PostMapping("/diagnose")
    DiagnosisReport diagnose(@PathVariable String sessionId) {
        return reportService.diagnose(sessionId);
    }

    @GetMapping("/report")
    DiagnosisReport latest(@PathVariable String sessionId) {
        return reportService.latest(sessionId);
    }

    @GetMapping("/diagnosis-progress")
    DiagnosisProgress progress(@PathVariable String sessionId) {
        return progressService.current(sessionId);
    }
}
