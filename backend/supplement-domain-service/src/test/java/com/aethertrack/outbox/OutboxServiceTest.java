package com.aethertrack.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    OutboxEventRepository outboxEventRepository;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    OutboxService outboxService;

    record SamplePayload(String key, int value) {}

    @Test
    void save_persistsOutboxEntryWithCorrectFields() {
        SamplePayload payload = new SamplePayload("test", 42);

        outboxService.save(
                "Regimen", "99", "RegimenCreated", "corr-123",
                "aethertrack.regimen.created", payload);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent saved = captor.getValue();
        assertThat(saved.getAggregateType()).isEqualTo("Regimen");
        assertThat(saved.getAggregateId()).isEqualTo("99");
        assertThat(saved.getEventType()).isEqualTo("RegimenCreated");
        assertThat(saved.getCorrelationId()).isEqualTo("corr-123");
        assertThat(saved.getTopic()).isEqualTo("aethertrack.regimen.created");
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getPayload()).contains("\"key\":\"test\"");
        assertThat(saved.getId()).isNotNull();
    }
}
