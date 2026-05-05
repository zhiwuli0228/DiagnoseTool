package com.geek.threaddoctor.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.geek.threaddoctor.common.ConfidenceLevel;
import com.geek.threaddoctor.common.ResourceNotFoundException;
import com.geek.threaddoctor.diagnosis.DiagnosisReport;
import com.geek.threaddoctor.diagnosis.DiagnosisReportService;
import com.geek.threaddoctor.prompt.PromptTestFactory;
import com.geek.threaddoctor.recovery.RecoveryActionService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentCardServiceTest {
    @Mock
    IncidentCardRepository repository;
    @Mock
    DiagnosisReportService diagnosisReportService;
    @Mock
    RecoveryActionService recoveryActionService;

    @Test
    void generatesMarkdownCardWithFaultTags() {
        when(diagnosisReportService.latest("INC-1")).thenReturn(new DiagnosisReport("RPT-1", "INC-1", "Redis exhausted", ConfidenceLevel.HIGH, "REDIS_POOL_EXHAUSTED"));
        when(recoveryActionService.list("INC-1")).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        IncidentCardService service = new IncidentCardService(repository, diagnosisReportService, recoveryActionService, PromptTestFactory.assemblyService());

        IncidentCard card = service.generate("INC-1");

        assertThat(card.getMarkdown()).contains("# Incident Result Document");
        assertThat(card.getTags()).contains("REDIS_POOL_EXHAUSTED");
    }

    @Test
    void readsLatestResultDocumentFromCache() {
        IncidentCard cached = new IncidentCard("CARD-1", "INC-1", "# Incident Result Document", "REDIS_POOL_EXHAUSTED");
        when(repository.findTopBySessionIdOrderByCreatedAtDesc("INC-1")).thenReturn(Optional.of(cached));
        IncidentCardService service = new IncidentCardService(repository, diagnosisReportService, recoveryActionService, PromptTestFactory.assemblyService());

        IncidentCard card = service.latest("INC-1");

        assertThat(card.getId()).isEqualTo("CARD-1");
    }

    @Test
    void reportsCacheMissForMissingResultDocument() {
        when(repository.findTopBySessionIdOrderByCreatedAtDesc("INC-404")).thenReturn(Optional.empty());
        IncidentCardService service = new IncidentCardService(repository, diagnosisReportService, recoveryActionService, PromptTestFactory.assemblyService());

        assertThatThrownBy(() -> service.latest("INC-404"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident result document not found");
    }
}
