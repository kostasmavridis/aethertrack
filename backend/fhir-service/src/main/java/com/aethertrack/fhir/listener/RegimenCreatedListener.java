package com.aethertrack.fhir.listener;

import com.aethertrack.fhir.events.DomainEvent;
import com.aethertrack.fhir.events.KafkaTopics;
import com.aethertrack.fhir.events.RegimenCreatedPayload;
import com.aethertrack.fhir.service.NutritionOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumes regimen.created events and triggers NutritionOrder creation.
 * Manual ACK – offset committed only on successful HAPI write + DB persist.
 */
@Slf4j
@Component
public class RegimenCreatedListener {

    private final NutritionOrderService nutritionOrderService;
    private final ObjectMapper          objectMapper;

    public RegimenCreatedListener(NutritionOrderService nutritionOrderService,
                                   ObjectMapper objectMapper) {
        this.nutritionOrderService = nutritionOrderService;
        this.objectMapper          = objectMapper;
    }

    @KafkaListener(
        topics           = KafkaTopics.REGIMEN_CREATED,
        groupId          = "${spring.kafka.consumer.group-id:fhir-service}",
        containerFactory = "fhirListenerContainerFactory"
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

            nutritionOrderService.createFromRegimen(event.payload(), correlationId);

            ack.acknowledge();
            log.info("[RegimenCreatedListener] ACK eventId={} regimenId={}",
                     event.eventId(), event.payload().regimenId());

        } catch (Exception ex) {
            log.error("[RegimenCreatedListener] FAILED offset={} correlationId={}: {}",
                      record.offset(), correlationId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to process RegimenCreated in fhir-service", ex);
        } finally {
            MDC.clear();
        }
    }
}
