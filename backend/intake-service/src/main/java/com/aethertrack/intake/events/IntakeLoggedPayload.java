package com.aethertrack.intake.events;

import java.math.BigDecimal;
import java.time.Instant;

public record IntakeLoggedPayload(
        Long intakeLogId,
        String patientId,
        Long regimenItemId,
        Instant takenDateTime,
        BigDecimal quantity
) {
}
