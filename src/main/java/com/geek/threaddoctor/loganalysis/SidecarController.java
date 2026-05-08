/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sidecar 本地日志分析接口。
 *
 * @author zhiwuli
 * @since 2026-05-08
 */
@RestController
@RequestMapping("/api/sidecar")
@Profile("sidecar")
public class SidecarController {
    private final SidecarLogAnalysisService service;

    /**
     * 创建 Sidecar 本地日志分析接口。
     *
     * @param service Sidecar 本地日志分析服务
     */
    public SidecarController(SidecarLogAnalysisService service) {
        this.service = service;
    }

    @GetMapping("/health")
    SidecarHealth health() {
        return service.health();
    }

    @PostMapping("/analysis/zip")
    SidecarAnalysisSnapshot analyzeZip(@Valid @RequestBody SidecarSourceRequest request) {
        return service.analyzeZip(request.path());
    }

    @PostMapping("/analysis/directory")
    SidecarAnalysisSnapshot analyzeDirectory(@Valid @RequestBody SidecarSourceRequest request) {
        return service.analyzeDirectory(request.path());
    }

    @PostMapping("/sessions/{sessionId}/search")
    LogSearchResult search(@PathVariable @Pattern(regexp = "LOCAL-[A-Za-z0-9-]{1,80}") String sessionId,
            @Valid @RequestBody(required = false) LogSearchRequest request) {
        return service.search(sessionId, request);
    }

    @GetMapping("/sessions/{sessionId}/snapshot")
    SidecarAnalysisSnapshot snapshot(@PathVariable @Pattern(regexp = "LOCAL-[A-Za-z0-9-]{1,80}") String sessionId) {
        return service.snapshot(sessionId);
    }
}
