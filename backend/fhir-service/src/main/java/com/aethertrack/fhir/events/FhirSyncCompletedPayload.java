package com.aethertrack.fhir.events;

import java.time.Instant;

/**
 * Event emitted by fhir-service when a NutritionOrder timing sync succeeds.
 */
public record FhirSyncCompletedPayload(
        Long regimenId,
        String nutritionOrderId,
        int supplementCount,
        Instant syncedAt
) {}
