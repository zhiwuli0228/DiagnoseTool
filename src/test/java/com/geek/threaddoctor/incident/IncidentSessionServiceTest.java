package com.geek.threaddoctor.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.common.SeverityLevel;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentSessionServiceTest {
    @Mock
    IncidentSessionRepository repository;

    @Test
    void createsIncidentWithCreatedStatus() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        IncidentSessionService service = new IncidentSessionService(repository);

        IncidentSession session = service.create("Redis timeout", "Errors increased", SeverityLevel.HIGH);

        assertThat(session.getId()).startsWith("INC-");
        assertThat(session.getStatus()).isEqualTo(IncidentStatus.CREATED);
        assertThat(session.getSeverity()).isEqualTo(SeverityLevel.HIGH);
    }

    @Test
    void reportsCacheMissForMissingSession() {
        when(repository.findById("INC-404")).thenReturn(Optional.empty());
        IncidentSessionService service = new IncidentSessionService(repository);

        assertThatThrownBy(() -> service.getRequired("INC-404"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident session not found");
    }
}
