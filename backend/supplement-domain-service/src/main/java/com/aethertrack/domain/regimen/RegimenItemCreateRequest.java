package com.aethertrack.domain.regimen;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RegimenItemCreateRequest(
        @NotNull(message = "supplementId is required")
        Long supplementId,

        @NotNull(message = "doseQty is required")
        @Positive(message = "doseQty must be > 0")
        BigDecimal doseQty,

        @NotNull(message = "doseUnit is required")
        String doseUnit,

        @NotNull(message = "frequencyPerDay is required")
        @Min(value = 1, message = "frequencyPerDay must be >= 1")
        Integer frequencyPerDay,

        String scheduleWindow
) {
}
