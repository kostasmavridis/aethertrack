package com.aethertrack.fhir.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "fhir_sync_outbox", schema = "fhir")
@Getter
@Setter
@NoArgsConstructor
public class FhirSyncOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "PENDING";

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public static FhirSyncOutboxEvent pending(String eventType, Long aggregateId,
                                              String correlationId, String payload) {
        FhirSyncOutboxEvent event = new FhirSyncOutboxEvent();
        event.setEventType(eventType);
        event.setAggregateId(aggregateId);
        event.setCorrelationId(correlationId);
        event.setPayload(payload);
        return event;
    }
}
