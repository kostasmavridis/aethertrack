package com.aethertrack.scheduling.listener;

import com.aethertrack.scheduling.config.KafkaTopics;
import com.aethertrack.scheduling.events.DomainEvent;
import com.aethertrack.scheduling.events.RegimenCreatedPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Listens to aethertrack.regimen.created.
 *
 * Slice 6: logs only.
 * Slice 8+: will map payload -> Timefold planning problem and run the solver.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegimenCreatedListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics           = KafkaTopics.REGIMEN_CREATED,
            groupId          = "${spring.kafka.consumer.group-id:scheduling-service}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onRegimenCreated(ConsumerRecord<String, DomainEvent<?>> record,
                                  Acknowledgment ack) {
        DomainEvent<?> envelope = record.value();
        log.info("[RegimenCreatedListener] Received event " +
                 "eventId={} eventType={} correlationId={} key={} partition={} offset={}",
                 envelope.eventId(), envelope.eventType(), envelope.correlationId(),
                 record.key(), record.partition(), record.offset());

        try {
            RegimenCreatedPayload payload = objectMapper.convertValue(
                    envelope.payload(), RegimenCreatedPayload.class);

            log.info("[RegimenCreatedListener] regimenId={} patientId={} items={}",
                     payload.regimenId(), payload.patientId(), payload.items().size());

            // TODO Slice 8: schedulingOrchestrator.schedule(payload);

            ack.acknowledge();

        } catch (Exception ex) {
            log.error("[RegimenCreatedListener] Failed to process event eventId={}: {}",
                      envelope.eventId(), ex.getMessage(), ex);
            // Do NOT ack – message redelivered (at-least-once semantics)
        }
    }
}
