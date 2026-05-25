package com.aethertrack.outbox;

import com.aethertrack.events.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Scheduled relay: polls PENDING outbox rows every ${outbox.poll.interval-ms} ms,
 * publishes each to Kafka synchronously, then marks SENT (or increments retryCount).
 * Batch size = 50 per poll cycle.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private static final int BATCH_SIZE = 50;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, DomainEvent<?>> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.poll.interval-ms:5000}")
    @Transactional
    public void pollAndRelay() {
        List<OutboxEvent> pending = outboxEventRepository
                .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PageRequest.of(0, BATCH_SIZE));

        if (pending.isEmpty()) return;

        log.debug("[OutboxPoller] Processing {} pending outbox events", pending.size());

        for (OutboxEvent entry : pending) {
            try {
                Object payloadObj = objectMapper.readValue(entry.getPayload(), Object.class);

                @SuppressWarnings("unchecked")
                DomainEvent<Object> event = new DomainEvent<>(
                        entry.getId().toString(),
                        entry.getEventType(),
                        entry.getVersion(),
                        entry.getCorrelationId(),
                        Instant.now(),
                        payloadObj
                );

                kafkaTemplate.send(entry.getTopic(), entry.getAggregateId(), event).get();

                entry.setStatus(OutboxStatus.SENT);
                entry.setProcessedAt(OffsetDateTime.now());
                outboxEventRepository.save(entry);

                log.info("[OutboxPoller] Relayed eventType={} aggregateId={} id={}",
                        entry.getEventType(), entry.getAggregateId(), entry.getId());

            } catch (Exception ex) {
                log.error("[OutboxPoller] Failed to relay outbox entry id={}: {}",
                        entry.getId(), ex.getMessage(), ex);
                entry.setRetryCount(entry.getRetryCount() + 1);
                entry.setLastError(ex.getMessage());
                outboxEventRepository.save(entry);
            }
        }
    }
}
