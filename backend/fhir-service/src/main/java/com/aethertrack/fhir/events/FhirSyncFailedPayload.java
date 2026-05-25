package com.aethertrack.fhir.events;

import java.time.Instant;

/**
 * Event emitted by fhir-service when timing sync fails.
 */
public record FhirSyncFailedPayload(
        Long regimenId,
        String nutritionOrderId,
        String reason,
        Instant failedAt
) {}
