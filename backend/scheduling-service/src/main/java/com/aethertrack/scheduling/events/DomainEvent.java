package com.aethertrack.scheduling.events;

import java.time.Instant;

/**
 * Generic inbound event envelope – mirrors the producer's DomainEvent<P>.
 * Payload is kept as Object here; each listener re-deserializes it to the concrete type.
 */
public record DomainEvent<P>(
        String eventId,
        String eventType,
        String version,
        String correlationId,
        Instant occurredAt,
        P payload
) {}
