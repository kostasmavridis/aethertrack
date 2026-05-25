package com.aethertrack.intake.events;

import java.time.Instant;

public record DomainEvent<P>(
        String eventId,
        String eventType,
        String version,
        Instant timestamp,
        String correlationId,
        String causationId,
        String userId,
        P payload
) {
}
