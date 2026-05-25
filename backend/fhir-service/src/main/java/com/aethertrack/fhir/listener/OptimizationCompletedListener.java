package com.aethertrack.fhir.listener;

import com.aethertrack.fhir.events.DomainEvent;
import com.aethertrack.fhir.events.KafkaTopics;
import com.aethertrack.fhir.events.OptimizationCompletedPayload;
import com.aethertrack.fhir.repository.RegimenFhirMappingRepository;
import com.aethertrack.fhir.service.NutritionOrderTimingSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OptimizationCompletedListener {

    private final NutritionOrderTimingSyncService timingSyncService;
    private final RegimenFhirMappingRepository mappingRepository;
    private final ObjectMapper objectMapper;

    public OptimizationCompletedListener(NutritionOrderTimingSyncService timingSyncService,
                                         RegimenFhirMappingRepository mappingRepository,
                                         ObjectMapper objectMapper) {
        this.timingSyncService = timingSyncService;
        this.mappingRepository = mappingRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.OPTIMIZATION_COMPLETED,
            groupId = "${spring.kafka.consumer.group-id:fhir-service}",
            containerFactory = "fhirListenerContainerFactory"
    )
    public void onOptimizationCompleted(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String correlationId = "unknown";
        Long regimenId = null;
        try {
            DomainEvent<OptimizationCompletedPayload> event = objectMapper.readValue(
                    record.value(),
                    objectMapper.getTypeFactory()
                            .constructParametricType(DomainEvent.class, OptimizationCompletedPayload.class));

            correlationId = event.correlationId() != null ? event.correlationId() : record.key();
            regimenId = event.payload().regimenId();

            MDC.put("correlationId", correlationId);
            MDC.put("regimenId", String.valueOf(regimenId));

            timingSyncService.syncTiming(event.payload(), correlationId);
            ack.acknowledge();

            log.info("[OptimizationCompletedListener] ACK regimenId={} offset={}", regimenId, record.offset());
        } catch (Exception ex) {
            String nutritionOrderId = regimenId == null ? null : mappingRepository.findByRegimenId(regimenId)
                    .map(m -> m.getNutritionOrderId())
                    .orElse(null);
            try {
                timingSyncService.enqueueFailed(regimenId, nutritionOrderId, ex.getMessage(), correlationId);
            } catch (Exception nested) {
                log.error("[OptimizationCompletedListener] failed to enqueue FHIRSyncFailed event: {}", nested.getMessage(), nested);
            }
            log.error("[OptimizationCompletedListener] FAILED offset={} regimenId={} correlationId={}: {}",
                    record.offset(), regimenId, correlationId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to process OptimizationCompleted in fhir-service", ex);
        } finally {
            MDC.clear();
        }
    }
}
