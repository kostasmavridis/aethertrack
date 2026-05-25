package com.aethertrack.events;

import java.util.List;

/**
 * Payload for the RegimenCreated domain event.
 * Immutable record — only exposes the fields consumers need.
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
            java.math.BigDecimal doseQty,
            String doseUnit,
            Integer frequencyPerDay,
            String scheduleWindow
    ) {}
}
