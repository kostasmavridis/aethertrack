package com.aethertrack.intake.events;

import java.time.Instant;

public record AdherenceEvaluatedPayload(
        Long   adherenceSummaryId,
        String patientId,
        Long   regimenItemId,
        Long   intakeLogId,
        String outcome,
        Integer deviationMins,
        Instant evaluatedAt
) {}
