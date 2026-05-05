package com.geek.threaddoctor.api;

import com.geek.threaddoctor.metrics.MetricsSnapshot;
import com.geek.threaddoctor.metrics.MetricsSnapshotService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents/{sessionId}/metrics")
public class MetricsController {
    private final MetricsSnapshotService service;

    public MetricsController(MetricsSnapshotService service) {
        this.service = service;
    }

    @PostMapping
    MetricsSnapshot save(@PathVariable String sessionId, @RequestBody MetricsRequest request) {
        return service.save(sessionId, request.jvmMetricsJson(), request.redisMetricsJson(), request.kafkaMetricsJson(), request.dbMetricsJson());
    }

    public record MetricsRequest(String jvmMetricsJson, String redisMetricsJson, String kafkaMetricsJson, String dbMetricsJson) {
    }
}
