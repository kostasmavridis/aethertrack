package com.aethertrack.scheduling.service;

import com.aethertrack.scheduling.domain.ScheduledDose;
import com.aethertrack.scheduling.domain.SupplementSchedule;
import com.aethertrack.scheduling.events.OptimizationCompletedPayload;
import com.aethertrack.scheduling.events.OptimizationCompletedPayload.DoseAssignment;
import com.aethertrack.scheduling.outbox.OutboxEvent;
import com.aethertrack.scheduling.outbox.OutboxEventRepository;
import com.aethertrack.scheduling.repository.ScheduledDoseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Persists the solved schedule and writes an OptimizationCompleted outbox
 * event in a single ACID transaction.
 *
 * <pre>
 * BEGIN
 *   DELETE old scheduled_dose rows for regimenId
 *   INSERT new scheduled_dose rows  (one per assigned dose)
 *   INSERT outbox_event (PENDING)   with OptimizationCompleted payload
 * COMMIT
 * </pre>
 *
 * {@link OutboxRelayService} picks up the PENDING row and publishes to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulePersistenceService {

    private final ScheduledDoseRepository scheduledDoseRepository;
    private final OutboxEventRepository   outboxEventRepository;
    private final ObjectMapper            objectMapper;

    @Transactional
    public void persistAndEnqueue(SupplementSchedule solution, String correlationId) {
        Long   regimenId = solution.getRegimenId();
        String patientId = solution.getPatientId();
        int    hard      = solution.getScore() != null ? solution.getScore().hardScore() : 0;
        int    soft      = solution.getScore() != null ? solution.getScore().softScore() : 0;

        // 1. Delete stale rows (re-solve scenario)
        int deleted = scheduledDoseRepository.deleteByRegimenId(regimenId);
        if (deleted > 0)
            log.info("[persist] Deleted {} stale rows for regimenId={}", deleted, regimenId);

        // 2. Persist new ScheduledDose rows
        List<ScheduledDose> rows = solution.getDoses().stream()
            .filter(d -> d.getAssignedSlot() != null)
            .map(d -> ScheduledDose.from(regimenId, d, hard, soft))
            .toList();
        scheduledDoseRepository.saveAll(rows);
        log.info("[persist] Saved {} scheduled_dose rows for regimenId={}", rows.size(), regimenId);

        // 3. Build OptimizationCompleted payload
        List<DoseAssignment> assignments = rows.stream()
            .map(sd -> new DoseAssignment(
                sd.getRegimenItemId(),
                solution.getDoses().stream()
                    .filter(d -> d.getRegimenItemId().equals(sd.getRegimenItemId()))
                    .findFirst()
                    .map(d -> d.getSupplementCode())
                    .orElse("UNKNOWN"),
                sd.getTimeslot(),
                sd.getTimeslotStart(),
                sd.getTimeslotEnd(),
                sd.getDayOffset()
            ))
            .toList();

        OptimizationCompletedPayload payload =
            new OptimizationCompletedPayload(regimenId, patientId, hard, soft, assignments);

        // 4. Write outbox entry – same transaction
        OutboxEvent entry = OutboxEvent.pending(
            "OptimizationCompleted", "Regimen",
            String.valueOf(regimenId), correlationId,
            toJson(payload));
        outboxEventRepository.save(entry);
        log.info("[persist] Outbox entry queued id={} regimenId={}", entry.getId(), regimenId);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialise outbox payload", e);
        }
    }
}
