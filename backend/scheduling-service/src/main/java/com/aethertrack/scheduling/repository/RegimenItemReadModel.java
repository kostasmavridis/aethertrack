package com.aethertrack.scheduling.repository;

import java.math.BigDecimal;

/**
 * Lightweight read model returned by {@link RegimenItemRepository}.
 * Mapped from the {@code scheduling.v_regimen_item} view.
 */
public record RegimenItemReadModel(
        Long itemId,
        Long regimenId,
        Long supplementId,
        String supplementCode,
        String supplementCategory,
        BigDecimal doseQty,
        String doseUnit,
        Integer frequencyPerDay,
        String scheduleWindow,
        boolean nightTimeRequired,
        boolean mealRequired
) {}
