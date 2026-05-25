package com.aethertrack.scheduling.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Polls {@code scheduling.outbox_event} every 2 s and publishes PENDING
 * events to Kafka, then marks them SENT (or FAILED after MAX_RETRY attempts).
 *
 * At-least-once delivery: consumers must deduplicate by eventId/aggregateId.
 *
 * Topic name convention: camelCase → dot.separated.lowercase
 *   e.g. "OptimizationCompleted" → "optimization.completed"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxRelayService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final int MAX_RETRY = 5;

    @Scheduled(fixedDelayString = "${aethertrack.outbox.poll-interval-ms:2000}")
    @Transactional
    public void relay() {
        List<OutboxEvent> pending = outboxEventRepository.findPendingEvents();
        if (pending.isEmpty()) return;

        log.debug("[OutboxRelay] Processing {} pending events", pending.size());

        for (OutboxEvent event : pending) {
            String topic = toTopicName(event.getEventType());
            try {
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload()).get();
                outboxEventRepository.markAs(event.getId(), OutboxStatus.SENT);
                log.info("[OutboxRelay] SENT eventType={} aggregateId={} topic={}",
                         event.getEventType(), event.getAggregateId(), topic);
            } catch (Exception ex) {
                int retries = event.getRetryCount() + 1;
                event.setRetryCount(retries);
                if (retries >= MAX_RETRY) {
                    outboxEventRepository.markAs(event.getId(), OutboxStatus.FAILED);
                    log.error("[OutboxRelay] FAILED after {} retries eventId={} topic={}",
                              retries, event.getId(), topic);
                } else {
                    outboxEventRepository.save(event);
                    log.warn("[OutboxRelay] Retry {}/{} eventId={}: {}",
                             retries, MAX_RETRY, event.getId(), ex.getMessage());
                }
            }
        }
    }

    /** "OptimizationCompleted" → "optimization.completed" */
    private String toTopicName(String eventType) {
        return eventType
            .replaceAll("([A-Z])", ".$1")
            .toLowerCase()
            .replaceFirst("^\\.", "");
    }
}
