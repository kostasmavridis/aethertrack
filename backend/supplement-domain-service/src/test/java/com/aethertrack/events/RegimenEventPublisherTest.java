package com.aethertrack.events;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegimenEventPublisherTest {

    @Mock
    KafkaTemplate<String, DomainEvent<?>> kafkaTemplate;

    @InjectMocks
    RegimenEventPublisher publisher;

    @Test
    @SuppressWarnings("unchecked")
    void publishRegimenCreated_sendsToCorrectTopicWithRegimenIdAsKey() {
        RecordMetadata meta = new RecordMetadata(
                new TopicPartition("aethertrack.regimen.created", 0),
                0L, 0, 0L, 0, 0);
        SendResult<String, DomainEvent<?>> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(meta);
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        RegimenCreatedPayload payload = new RegimenCreatedPayload(
                42L, "patient-123", "Morning stack",
                List.of(new RegimenCreatedPayload.RegimenItemPayload(
                        100L, 1L, "VIT-D3-1000", BigDecimal.ONE, "tablet", 1, "MORNING")));

        publisher.publishRegimenCreated(payload);

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(kafkaTemplate).send(
                eq("aethertrack.regimen.created"), eq("42"), eventCaptor.capture());

        DomainEvent<?> captured = eventCaptor.getValue();
        assertThat(captured.eventType()).isEqualTo("RegimenCreated");
        assertThat(captured.version()).isEqualTo("v1");
        assertThat(captured.eventId()).isNotBlank();
        assertThat(captured.occurredAt()).isNotNull();

        RegimenCreatedPayload capturedPayload = (RegimenCreatedPayload) captured.payload();
        assertThat(capturedPayload.regimenId()).isEqualTo(42L);
        assertThat(capturedPayload.items().get(0).supplementCode()).isEqualTo("VIT-D3-1000");
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishRegimenCreated_logsWarnOnFailure_doesNotThrow() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka down")));

        publisher.publishRegimenCreated(
                new RegimenCreatedPayload(99L, "patient-x", "Stack", List.of()));

        verify(kafkaTemplate).send(eq("aethertrack.regimen.created"), eq("99"), any());
    }
}
