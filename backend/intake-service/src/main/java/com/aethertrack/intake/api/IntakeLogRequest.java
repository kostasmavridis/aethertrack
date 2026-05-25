package com.aethertrack.intake.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record IntakeLogRequest(
        @NotBlank String patientId,
        @NotNull Long regimenItemId,
        @NotNull Instant takenDateTime,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantity
) {
}
