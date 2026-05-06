package com.geek.threaddoctor.api;

import com.geek.threaddoctor.metrics.MetricsSnapshot;
import com.geek.threaddoctor.metrics.MetricsSnapshotService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/incidents/{sessionId}/metrics")
@Validated
public class MetricsController {
    private final MetricsSnapshotService service;

    public MetricsController(MetricsSnapshotService service) {
        this.service = service;
    }

    @PostMapping
    MetricsSnapshot save(@PathVariable @Pattern(regexp = "INC-[A-Za-z0-9-]{1,80}") String sessionId,
            @Valid @RequestBody MetricsRequest request) {
        return service.save(sessionId, request.jvmMetricsJson(), request.redisMetricsJson(), request.kafkaMetricsJson(), request.dbMetricsJson());
    }

    public record MetricsRequest(
            @Size(max = 100000) String jvmMetricsJson,
            @Size(max = 100000) String redisMetricsJson,
            @Size(max = 100000) String kafkaMetricsJson,
            @Size(max = 100000) String dbMetricsJson) {
    }
}
