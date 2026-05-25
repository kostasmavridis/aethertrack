package com.aethertrack.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Generic event envelope shared by all domain events.
 *
 * @param <P> payload type
 */
public record DomainEvent<P>(
        String eventId,         // UUID, generated at publish time
        String eventType,       // e.g. "RegimenCreated"
        String version,         // schema version, e.g. "v1"
        String correlationId,   // request-scoped trace id
        Instant occurredAt,     // UTC wall-clock
        P payload               // event-specific payload
) {
    public static <P> DomainEvent<P> of(String eventType, String correlationId, P payload) {
        return new DomainEvent<>(
                UUID.randomUUID().toString(),
                eventType,
                "v1",
                correlationId,
                Instant.now(),
                payload
        );
    }
}
