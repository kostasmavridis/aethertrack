package com.aethertrack.intake.service;

import com.aethertrack.intake.domain.AdherenceSummary;
import com.aethertrack.intake.domain.OutboxEvent;
import com.aethertrack.intake.domain.ScheduledDoseRef;
import com.aethertrack.intake.events.AdherenceEvaluatedPayload;
import com.aethertrack.intake.events.DomainEvent;
import com.aethertrack.intake.events.KafkaTopics;
import com.aethertrack.intake.repository.AdherenceSummaryRepository;
import com.aethertrack.intake.repository.OutboxEventRepository;
import com.aethertrack.intake.repository.ScheduledDoseRefRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

/**
 * Evaluates adherence for a single IntakeLogged event.
 *
 * Outcome logic:
 *   Given an intake at localTime T and a window [start, end]:
 *   - ON_TIME:      start <= T <= end
 *   - EARLY:        T < start
 *   - LATE:         T > end
 *   - UNSCHEDULED:  no ScheduledDoseRef for the regimenItemId
 *
 * deviationMins is signed: negative = early, positive = late, 0 = on-time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdherenceEvaluationService {

    private final ScheduledDoseRefRepository  scheduleRepo;
    private final AdherenceSummaryRepository  summaryRepo;
    private final OutboxEventRepository       outboxRepo;
    private final ObjectMapper                objectMapper;

    @Transactional
    public AdherenceSummary evaluate(String patientId, Long regimenItemId, Long intakeLogId,
                                     Instant takenDateTime, String correlationId) {

        LocalTime takenTime = takenDateTime.atZone(ZoneOffset.UTC).toLocalTime();

        Optional<ScheduledDoseRef> refOpt = scheduleRepo.findFirstByRegimenItemId(regimenItemId);

        String outcome;
        Integer deviationMins;

        if (refOpt.isEmpty()) {
            outcome       = "UNSCHEDULED";
            deviationMins = null;
            log.warn("[AdherenceEvaluationService] No schedule ref for regimenItemId={}", regimenItemId);
        } else {
            ScheduledDoseRef ref = refOpt.get();
            int devStart = (int) java.time.Duration.between(ref.getWindowStartTime(), takenTime).toMinutes();
            int devEnd   = (int) java.time.Duration.between(ref.getWindowEndTime(),   takenTime).toMinutes();

            if (!takenTime.isBefore(ref.getWindowStartTime()) && !takenTime.isAfter(ref.getWindowEndTime())) {
                outcome       = "ON_TIME";
                deviationMins = 0;
            } else if (takenTime.isBefore(ref.getWindowStartTime())) {
                outcome       = "EARLY";
                deviationMins = devStart;     // negative
            } else {
                outcome       = "LATE";
                deviationMins = devEnd;       // positive
            }
        }

        AdherenceSummary summary = AdherenceSummary.of(
                patientId, regimenItemId, intakeLogId, outcome, deviationMins);
        AdherenceSummary saved = summaryRepo.save(summary);

        enqueueAdherenceEvaluated(saved, correlationId);

        log.info("[AdherenceEvaluationService] outcome={} deviation={}min regimenItemId={}",
                outcome, deviationMins, regimenItemId);
        return saved;
    }

    private void enqueueAdherenceEvaluated(AdherenceSummary saved, String correlationId) {
        try {
            var event = new DomainEvent<>(
                    UUID.randomUUID().toString(),
                    "AdherenceEvaluated",
                    "1",
                    Instant.now(),
                    correlationId,
                    null,
                    saved.getPatientId(),
                    new AdherenceEvaluatedPayload(
                            saved.getId(),
                            saved.getPatientId(),
                            saved.getRegimenItemId(),
                            saved.getIntakeLogId(),
                            saved.getOutcome(),
                            saved.getDeviationMins(),
                            saved.getEvaluatedAt()
                    )
            );
            String json = objectMapper.writeValueAsString(event);
            outboxRepo.save(OutboxEvent.pending(
                    KafkaTopics.ADHERENCE_EVALUATED,
                    saved.getId(),
                    correlationId,
                    json
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize AdherenceEvaluated event", e);
        }
    }
}
