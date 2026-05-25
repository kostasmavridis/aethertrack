package com.aethertrack.intake.service;

import com.aethertrack.intake.domain.OutboxEvent;
import com.aethertrack.intake.repository.OutboxEventRepository;
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
class OutboxRelayServiceTest {

    @Mock OutboxEventRepository outboxEventRepository;
    @Mock KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks OutboxRelayService relayService;

    @Test
    void relayPending_sendsAndMarksSent() throws Exception {
        OutboxEvent event = OutboxEvent.pending("intake.logged", 1L, "corr-1", "{}");
        event.setId(1L);
        when(outboxEventRepository.findTop20ByStatusOrderByCreatedAtAsc("PENDING")).thenReturn(List.of(event));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));

        relayService.relayPending();

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("SENT");
    }
}
