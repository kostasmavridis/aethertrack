package com.aethertrack.outbox;

import com.aethertrack.events.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPollerIT {

    @Mock OutboxEventRepository outboxEventRepository;
    @Mock KafkaTemplate<String, DomainEvent<?>> kafkaTemplate;
    @Spy  ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks OutboxPoller outboxPoller;

    private OutboxEvent buildEntry(Long regimenId, int retryCount) {
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType("Regimen")
                .aggregateId(regimenId.toString())
                .eventType("RegimenCreated")
                .correlationId("corr-" + regimenId)
                .topic("aethertrack.regimen.created")
                .payload("{\"regimenId\":" + regimenId + "}")
                .status(OutboxStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .retryCount(retryCount)
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void pollAndRelay_sendsPendingEventAndMarksSent() throws Exception {
        OutboxEvent entry = buildEntry(42L, 0);
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxStatus.PENDING), any(Pageable.class)))
                .thenReturn(List.of(entry));
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        outboxPoller.pollAndRelay();

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        OutboxEvent updated = captor.getValue();
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(updated.getProcessedAt()).isNotNull();
        assertThat(updated.getRetryCount()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void pollAndRelay_onKafkaFailure_incrementsRetryCountStaysPending() {
        OutboxEvent entry = buildEntry(77L, 0);
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                eq(OutboxStatus.PENDING), any(Pageable.class)))
                .thenReturn(List.of(entry));
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka unavailable")));

        outboxPoller.pollAndRelay();

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        OutboxEvent updated = captor.getValue();
        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(updated.getRetryCount()).isEqualTo(1);
        assertThat(updated.getLastError()).contains("Kafka unavailable");
    }
}
