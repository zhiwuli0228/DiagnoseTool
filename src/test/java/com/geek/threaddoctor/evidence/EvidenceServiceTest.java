package com.geek.threaddoctor.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geek.threaddoctor.common.SeverityLevel;
import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.incident.IncidentSession;
import com.geek.threaddoctor.incident.IncidentSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {
    @Mock
    EvidenceRepository repository;
    @Mock
    IncidentSessionService incidentSessionService;

    @Test
    void uploadsEvidenceOnlyAfterSessionValidation() {
        when(incidentSessionService.getRequired("INC-1")).thenReturn(new IncidentSession("INC-1", "t", "d", SeverityLevel.MEDIUM));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        EvidenceService service = new EvidenceService(repository, incidentSessionService);

        Evidence evidence = service.upload("INC-1", EvidenceType.LOG_SNIPPET, "manual", "error", "{}");

        verify(incidentSessionService).getRequired("INC-1");
        assertThat(evidence.getId()).startsWith("EVD-");
        assertThat(evidence.getSessionId()).isEqualTo("INC-1");
    }

    @Test
    void refusesEvidenceWhenSessionCacheMisses() {
        when(incidentSessionService.getRequired("INC-404")).thenThrow(new ResourceNotFoundException("missing"));
        EvidenceService service = new EvidenceService(repository, incidentSessionService);

        assertThatThrownBy(() -> service.upload("INC-404", EvidenceType.LOG_SNIPPET, "manual", "error", "{}"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
