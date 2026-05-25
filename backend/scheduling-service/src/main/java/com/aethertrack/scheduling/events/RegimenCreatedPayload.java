package com.aethertrack.scheduling.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * Consumer-side mirror of the RegimenCreated event payload.
 */
public record RegimenCreatedPayload(
        Long regimenId,
        String patientId,
        String name,
        List<RegimenItemPayload> items
) {
    public record RegimenItemPayload(
            Long itemId,
            Long supplementId,
            String supplementCode,
            BigDecimal doseQty,
            String doseUnit,
            Integer frequencyPerDay,
            String scheduleWindow
    ) {}
}
