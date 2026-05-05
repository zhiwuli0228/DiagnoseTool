package com.geek.threaddoctor.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geek.threaddoctor.common.SeverityLevel;
import com.geek.threaddoctor.incident.IncidentSession;
import com.geek.threaddoctor.incident.IncidentSessionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsSnapshotServiceTest {
    @Mock
    MetricsSnapshotRepository repository;
    @Mock
    IncidentSessionService incidentSessionService;

    @Test
    void cachesMetricsOnlyAfterSessionValidation() {
        when(incidentSessionService.getRequired("INC-1")).thenReturn(new IncidentSession("INC-1", "t", "d", SeverityLevel.HIGH));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MetricsSnapshotService service = new MetricsSnapshotService(repository, incidentSessionService);

        MetricsSnapshot snapshot = service.save("INC-1", "{\"heapUsed\":1}", "{\"active\":10}", "{}", "{}");

        verify(incidentSessionService).getRequired("INC-1");
        assertThat(snapshot.getId()).startsWith("MTR-");
        assertThat(snapshot.getRedisMetricsJson()).contains("active");
    }

    @Test
    void readsCachedMetricsForSession() {
        when(incidentSessionService.getRequired("INC-1")).thenReturn(new IncidentSession("INC-1", "t", "d", SeverityLevel.HIGH));
        when(repository.findBySessionId("INC-1")).thenReturn(List.of(new MetricsSnapshot("MTR-1", "INC-1", "{}", "{}", "{}", "{}")));
        MetricsSnapshotService service = new MetricsSnapshotService(repository, incidentSessionService);

        List<MetricsSnapshot> snapshots = service.listBySession("INC-1");

        assertThat(snapshots).hasSize(1);
    }
}
