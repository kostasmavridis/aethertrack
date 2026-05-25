package com.aethertrack.intake.api;

import java.math.BigDecimal;
import java.time.Instant;

public record IntakeLogResponse(
        Long id,
        String patientId,
        Long regimenItemId,
        Instant takenDateTime,
        BigDecimal quantity,
        String status
) {
}
