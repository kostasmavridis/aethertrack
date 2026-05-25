package com.aethertrack.scheduling.events;

import java.time.LocalTime;
import java.util.List;

/**
 * Payload for the OptimizationCompleted domain event.
 *
 * Published via the transactional outbox after the Timefold solver
 * finishes and schedule rows are persisted.
 *
 * Consumed by:
 *   - fhir-service  (Slice 12): update NutritionOrder Timing fields
 *   - frontend       (Slice 16): render schedule timeline + score breakdown
 */
public record OptimizationCompletedPayload(
        Long   regimenId,
        String patientId,
        int    hardScore,
        int    softScore,
        List<DoseAssignment> assignments
) {
    public record DoseAssignment(
            Long      regimenItemId,
            String    supplementCode,
            String    timeslot,
            LocalTime timeslotStart,
            LocalTime timeslotEnd,
            int       dayOffset
    ) {}
}
