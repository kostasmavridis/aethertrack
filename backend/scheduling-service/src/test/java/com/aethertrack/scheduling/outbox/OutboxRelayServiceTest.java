package com.aethertrack.scheduling.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayServiceTest {

    @Mock OutboxEventRepository outboxEventRepository;
    @Mock KafkaTemplate<String, String> kafkaTemplate;
    @InjectMocks OutboxRelayService relayService;

    private OutboxEvent pending(String eventType, String aggregateId) {
        OutboxEvent e = OutboxEvent.pending(eventType, "Regimen", aggregateId, "corr-1", "{}");
        try {
            var f = OutboxEvent.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(e, UUID.randomUUID());
        } catch (Exception ex) { /* test setup */ }
        return e;
    }

    @Test void relay_sendsToCorrectTopicAndMarksSent() throws Exception {
        OutboxEvent evt = pending("OptimizationCompleted", "42");
        when(outboxEventRepository.findPendingEvents()).thenReturn(List.of(evt));
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future =
            CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        relayService.relay();

        verify(kafkaTemplate).send(eq("optimization.completed"), eq("42"), anyString());
        verify(outboxEventRepository).markAs(evt.getId(), OutboxStatus.SENT);
    }

    @Test void relay_doesNothing_whenNoPendingEvents() {
        when(outboxEventRepository.findPendingEvents()).thenReturn(List.of());
        relayService.relay();
        verifyNoInteractions(kafkaTemplate);
    }
}
