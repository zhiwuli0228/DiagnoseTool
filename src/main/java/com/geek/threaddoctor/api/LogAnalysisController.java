package com.geek.threaddoctor.api;

import com.geek.threaddoctor.loganalysis.CodexTask;
import com.geek.threaddoctor.loganalysis.DirectoryScanRequest;
import com.geek.threaddoctor.loganalysis.EvidencePack;
import com.geek.threaddoctor.loganalysis.IncidentTimeline;
import com.geek.threaddoctor.loganalysis.LogAnalysisService;
import com.geek.threaddoctor.loganalysis.LogAnalysisSession;
import com.geek.threaddoctor.loganalysis.LogCluster;
import com.geek.threaddoctor.loganalysis.LogSearchRequest;
import com.geek.threaddoctor.loganalysis.LogSearchResult;
import com.geek.threaddoctor.loganalysis.OpenSpecChangeDraft;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/log-analysis/sessions")
public class LogAnalysisController {
    private final LogAnalysisService service;

    public LogAnalysisController(LogAnalysisService service) {
        this.service = service;
    }

    @PostMapping
    LogAnalysisSession create() {
        return service.createSession();
    }

    @GetMapping("/{sessionId}")
    LogAnalysisSession get(@PathVariable String sessionId) {
        return service.getSession(sessionId);
    }

    @PostMapping(path = "/{sessionId}/zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    LogAnalysisSession uploadZip(@PathVariable String sessionId, @RequestParam("file") MultipartFile file) {
        return service.uploadZip(sessionId, file);
    }

    @PostMapping(path = "/{sessionId}/directory", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    LogAnalysisSession uploadDirectory(@PathVariable String sessionId, @RequestParam("files") MultipartFile[] files) {
        return service.uploadDirectoryFiles(sessionId, files);
    }

    @PostMapping("/{sessionId}/directory-scan")
    LogAnalysisSession scanDirectory(@PathVariable String sessionId, @Valid @RequestBody DirectoryScanRequest request) {
        return service.scanDirectory(sessionId, request.path());
    }

    @PostMapping("/{sessionId}/search")
    LogSearchResult search(@PathVariable String sessionId, @RequestBody(required = false) LogSearchRequest request) {
        return service.search(sessionId, request);
    }

    @GetMapping("/{sessionId}/clusters")
    List<LogCluster> clusters(@PathVariable String sessionId) {
        return service.clusters(sessionId);
    }

    @GetMapping("/{sessionId}/timeline")
    IncidentTimeline timeline(@PathVariable String sessionId) {
        return service.timeline(sessionId);
    }

    @GetMapping("/{sessionId}/evidence-pack")
    ResponseEntity<?> evidencePack(@PathVariable String sessionId,
            @RequestParam(defaultValue = "json") String format) {
        if ("markdown".equalsIgnoreCase(format)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_MARKDOWN)
                    .body(service.evidencePackMarkdown(sessionId));
        }
        EvidencePack pack = service.evidencePack(sessionId);
        return ResponseEntity.ok(pack);
    }

    @PostMapping("/{sessionId}/codex-task")
    CodexTask codexTask(@PathVariable String sessionId) {
        return service.codexTask(sessionId);
    }

    @PostMapping("/{sessionId}/openspec-change-draft")
    OpenSpecChangeDraft openSpecChangeDraft(@PathVariable String sessionId) {
        return service.openSpecChangeDraft(sessionId);
    }
}
