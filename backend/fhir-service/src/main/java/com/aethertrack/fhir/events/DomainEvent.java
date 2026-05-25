package com.aethertrack.fhir.events;

import java.time.Instant;

/** Generic event envelope shared across all domain events. */
public record DomainEvent<P>(
        String  eventId,
        String  eventType,
        String  version,
        Instant timestamp,
        String  correlationId,
        String  causationId,
        String  userId,
        P       payload
) {}
