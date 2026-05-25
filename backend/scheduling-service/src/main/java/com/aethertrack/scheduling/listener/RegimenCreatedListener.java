package com.aethertrack.scheduling.listener;

import com.aethertrack.scheduling.events.DomainEvent;
import com.aethertrack.scheduling.events.RegimenCreatedPayload;
import com.aethertrack.scheduling.service.SchedulingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for regimen.created.
 * Passes correlationId through to SchedulingService (Slice 9: stored in outbox row).
 */
@Slf4j
@Component
public class RegimenCreatedListener {

    private final SchedulingService schedulingService;
    private final ObjectMapper objectMapper;

    public RegimenCreatedListener(SchedulingService schedulingService,
                                   ObjectMapper objectMapper) {
        this.schedulingService = schedulingService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics           = "${aethertrack.topics.regimen-created:regimen.created}",
        groupId          = "${spring.kafka.consumer.group-id:scheduling-service}",
        containerFactory = "regimenCreatedListenerContainerFactory"
    )
    public void onRegimenCreated(ConsumerRecord<String, String> record,
                                  Acknowledgment ack) {
        String correlationId = "unknown";
        try {
            DomainEvent<RegimenCreatedPayload> event = objectMapper.readValue(
                record.value(),
                objectMapper.getTypeFactory()
                    .constructParametricType(DomainEvent.class, RegimenCreatedPayload.class));

            correlationId = event.correlationId() != null ? event.correlationId() : record.key();
            MDC.put("correlationId", correlationId);
            MDC.put("regimenId",     String.valueOf(event.payload().regimenId()));

            log.info("[RegimenCreatedListener] eventId={} regimenId={} items={} offset={}",
                     event.eventId(), event.payload().regimenId(),
                     event.payload().items().size(), record.offset());

            schedulingService.scheduleRegimen(event.payload(), correlationId);

            ack.acknowledge();
            log.info("[RegimenCreatedListener] ACK eventId={}", event.eventId());

        } catch (Exception ex) {
            log.error("[RegimenCreatedListener] Failed offset={} correlationId={}: {}",
                      record.offset(), correlationId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to process RegimenCreated event", ex);
        } finally {
            MDC.clear();
        }
    }
}
