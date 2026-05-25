package com.aethertrack.events;

import com.aethertrack.config.CorrelationIdHolder;
import com.aethertrack.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegimenEventPublisher {

    private final KafkaTemplate<String, DomainEvent<?>> kafkaTemplate;

    /**
     * Publishes a RegimenCreated event.
     * Key = regimenId (ensures ordering per-regimen within a partition).
     * NOTE: fire-and-forget with completion callback. Outbox pattern in Slice 5.
     */
    public void publishRegimenCreated(RegimenCreatedPayload payload) {
        DomainEvent<RegimenCreatedPayload> event =
                DomainEvent.of("RegimenCreated", CorrelationIdHolder.get(), payload);

        String key = payload.regimenId().toString();
        CompletableFuture<SendResult<String, DomainEvent<?>>> future =
                kafkaTemplate.send(KafkaTopics.REGIMEN_CREATED, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[RegimenEventPublisher] Failed to publish RegimenCreated " +
                          "regimenId={} correlationId={}: {}",
                          payload.regimenId(), event.correlationId(), ex.getMessage(), ex);
            } else {
                log.info("[RegimenEventPublisher] Published RegimenCreated " +
                         "regimenId={} correlationId={} offset={}",
                         payload.regimenId(), event.correlationId(),
                         result.getRecordMetadata().offset());
            }
        });
    }
}
