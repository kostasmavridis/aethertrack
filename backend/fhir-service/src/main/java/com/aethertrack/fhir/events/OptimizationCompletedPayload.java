package com.aethertrack.fhir.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Payload emitted by scheduling-service once a regimen has been solved.
 */
public record OptimizationCompletedPayload(
        Long regimenId,
        String score,
        Instant optimizedAt,
        List<DoseAssignment> assignments
) {
    public record DoseAssignment(
            Long regimenItemId,
            Integer dayOffset,
            String timeslotCode,
            String startTime,
            String endTime,
            BigDecimal quantity,
            String unit
    ) {}
}
