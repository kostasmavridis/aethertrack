package com.aethertrack.fhir.service;

import com.aethertrack.fhir.domain.FhirSyncOutboxEvent;
import com.aethertrack.fhir.repository.FhirSyncOutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FhirSyncOutboxRelayServiceTest {

    @Mock FhirSyncOutboxRepository outboxRepository;
    @Mock KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks FhirSyncOutboxRelayService relayService;

    @Test
    void relayPending_sendsAndMarksSent() throws Exception {
        FhirSyncOutboxEvent event = FhirSyncOutboxEvent.pending("fhir.sync.completed", 42L, "corr-1", "{}");
        event.setId(1L);
        when(outboxRepository.findTop20ByStatusOrderByCreatedAtAsc("PENDING")).thenReturn(List.of(event));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));

        relayService.relayPending();

        ArgumentCaptor<FhirSyncOutboxEvent> captor = ArgumentCaptor.forClass(FhirSyncOutboxEvent.class);
        verify(outboxRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("SENT");
    }
}
