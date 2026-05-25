package com.aethertrack.intake.listener;

import com.aethertrack.intake.events.DomainEvent;
import com.aethertrack.intake.events.IntakeLoggedPayload;
import com.aethertrack.intake.events.KafkaTopics;
import com.aethertrack.intake.service.AdherenceEvaluationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumes intake.logged events and triggers adherence evaluation.
 * Runs in the same intake-service process, sharing the same DB transaction scope.
 */
@Slf4j
@Component
public class IntakeLoggedListener {

    private final AdherenceEvaluationService evaluationService;
    private final ObjectMapper               objectMapper;

    public IntakeLoggedListener(AdherenceEvaluationService evaluationService,
                                 ObjectMapper objectMapper) {
        this.evaluationService = evaluationService;
        this.objectMapper      = objectMapper;
    }

    @KafkaListener(
            topics           = KafkaTopics.INTAKE_LOGGED,
            groupId          = "${spring.kafka.consumer.group-id:intake-adherence}",
            containerFactory = "intakeListenerContainerFactory"
    )
    public void onIntakeLogged(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String correlationId = "unknown";
        try {
            DomainEvent<IntakeLoggedPayload> event = objectMapper.readValue(
                    record.value(),
                    objectMapper.getTypeFactory()
                            .constructParametricType(DomainEvent.class, IntakeLoggedPayload.class));

            correlationId = event.correlationId() != null ? event.correlationId() : record.key();
            MDC.put("correlationId", correlationId);
            MDC.put("regimenItemId", String.valueOf(event.payload().regimenItemId()));

            log.info("[IntakeLoggedListener] eventId={} intakeLogId={} offset={}",
                    event.eventId(), event.payload().intakeLogId(), record.offset());

            evaluationService.evaluate(
                    event.payload().patientId(),
                    event.payload().regimenItemId(),
                    event.payload().intakeLogId(),
                    event.payload().takenDateTime(),
                    correlationId
            );

            ack.acknowledge();
            log.info("[IntakeLoggedListener] ACK eventId={} intakeLogId={}",
                    event.eventId(), event.payload().intakeLogId());
        } catch (Exception ex) {
            log.error("[IntakeLoggedListener] FAILED offset={} correlationId={}: {}",
                    record.offset(), correlationId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to process IntakeLogged", ex);
        } finally {
            MDC.clear();
        }
    }
}
