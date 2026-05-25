package com.aethertrack.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Write-side of the outbox. Must always be called from within
 * the same transaction as the business operation so that the
 * outbox row and the domain row commit atomically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(String aggregateType,
                     String aggregateId,
                     String eventType,
                     String correlationId,
                     String topic,
                     Object payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Cannot serialize outbox payload for " + eventType, e);
        }

        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .correlationId(correlationId)
                .topic(topic)
                .payload(json)
                .build();

        outboxEventRepository.save(event);
        log.debug("[OutboxService] Persisted outbox entry eventType={} aggregateId={}",
                eventType, aggregateId);
    }
}
