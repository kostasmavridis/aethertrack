package com.aethertrack.fhir.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.aethertrack.fhir.domain.FhirSyncOutboxEvent;
import com.aethertrack.fhir.domain.RegimenFhirMapping;
import com.aethertrack.fhir.events.*;
import com.aethertrack.fhir.repository.FhirSyncOutboxRepository;
import com.aethertrack.fhir.repository.RegimenFhirMappingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r5.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionOrderTimingSyncService {

    private final IGenericClient fhirClient;
    private final RegimenFhirMappingRepository mappingRepository;
    private final FhirSyncOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void syncTiming(OptimizationCompletedPayload payload, String correlationId) {
        RegimenFhirMapping mapping = mappingRepository.findByRegimenId(payload.regimenId())
                .orElseThrow(() -> new IllegalStateException(
                        "No regimen_fhir_mapping for regimenId=" + payload.regimenId()));

        String nutritionOrderId = mapping.getNutritionOrderId();

        NutritionOrder order = fhirClient.read()
                .resource(NutritionOrder.class)
                .withId(nutritionOrderId)
                .execute();

        Map<String, List<OptimizationCompletedPayload.DoseAssignment>> bySlot = payload.assignments().stream()
                .collect(Collectors.groupingBy(OptimizationCompletedPayload.DoseAssignment::timeslotCode));

        List<Timing.EventTiming> whenCodes = bySlot.keySet().stream()
                .map(this::mapTimeslotCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        for (NutritionOrder.NutritionOrderSupplementComponent supplement : order.getSupplement()) {
            if (!supplement.hasSchedule()) {
                supplement.addSchedule();
            }
            Timing timing = new Timing();
            Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();
            repeat.setFrequency(Math.max(1, whenCodes.size()));
            repeat.setPeriod(1);
            repeat.setPeriodUnit(Timing.UnitsOfTime.D);
            whenCodes.forEach(repeat::addWhen);
            timing.setRepeat(repeat);
            supplement.getScheduleFirstRep().setTiming(timing);
        }

        MethodOutcome outcome = fhirClient.update()
                .resource(order)
                .execute();

        mapping.setUpdatedAt(Instant.now());
        mappingRepository.save(mapping);

        enqueueCompleted(payload.regimenId(), nutritionOrderId, order.getSupplement().size(), correlationId);

        log.info("[NutritionOrderTimingSyncService] Updated NutritionOrder id={} regimenId={} version={}",
                nutritionOrderId,
                payload.regimenId(),
                outcome.getId() != null ? outcome.getId().getVersionIdPart() : "n/a");
    }

    @Transactional
    public void enqueueFailed(Long regimenId, String nutritionOrderId, String reason, String correlationId) {
        try {
            var completed = new DomainEvent<>(
                    UUID.randomUUID().toString(),
                    "FHIRSyncFailed",
                    "1",
                    Instant.now(),
                    correlationId,
                    null,
                    null,
                    new FhirSyncFailedPayload(regimenId, nutritionOrderId, reason, Instant.now())
            );
            String json = objectMapper.writeValueAsString(completed);
            outboxRepository.save(FhirSyncOutboxEvent.pending(KafkaTopics.FHIR_SYNC_FAILED, regimenId, correlationId, json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize FHIRSyncFailed payload", e);
        }
    }

    private void enqueueCompleted(Long regimenId, String nutritionOrderId, int supplementCount, String correlationId) {
        try {
            var completed = new DomainEvent<>(
                    UUID.randomUUID().toString(),
                    "FHIRSyncCompleted",
                    "1",
                    Instant.now(),
                    correlationId,
                    null,
                    null,
                    new FhirSyncCompletedPayload(regimenId, nutritionOrderId, supplementCount, Instant.now())
            );
            String json = objectMapper.writeValueAsString(completed);
            outboxRepository.save(FhirSyncOutboxEvent.pending(KafkaTopics.FHIR_SYNC_COMPLETED, regimenId, correlationId, json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize FHIRSyncCompleted payload", e);
        }
    }

    private Timing.EventTiming mapTimeslotCode(String code) {
        if (code == null) return null;
        return switch (code.toUpperCase()) {
            case "MORNING", "AM", "BREAKFAST" -> Timing.EventTiming.MORN;
            case "MIDDAY", "NOON", "LUNCH" -> Timing.EventTiming.NOON;
            case "EVENING", "PM", "DINNER" -> Timing.EventTiming.EVE;
            case "NIGHT", "BEDTIME" -> Timing.EventTiming.HS;
            case "WITH_MEAL", "MEAL" -> Timing.EventTiming.PCM;
            default -> null;
        };
    }
}
