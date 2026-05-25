package com.aethertrack.fhir.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payload of the RegimenCreated domain event as consumed by fhir-service.
 * Must stay structurally compatible with the supplement-domain-service publisher.
 */
public record RegimenCreatedPayload(
        Long               regimenId,
        String             patientId,
        String             name,
        List<RegimenItem>  items
) {
    public record RegimenItem(
            Long       itemId,
            Long       supplementId,
            String     supplementCode,
            BigDecimal doseQty,
            String     doseUnit,
            Integer    frequencyPerDay,
            String     scheduleWindow    // MORNING | MIDDAY | EVENING | NIGHT | WITH_MEAL | null
    ) {}
}
