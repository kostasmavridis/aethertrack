package com.aethertrack.scheduling.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the transactional outbox table.
 * Written in the same DB transaction as {@code scheduled_dose} rows,
 * then relayed to Kafka by {@link OutboxRelayService}.
 */
@Entity
@Table(name = "outbox_event", schema = "scheduling")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_type",     nullable = false, length = 64)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id",   nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "payload",        nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "status",         nullable = false, length = 16)
    private String status = OutboxStatus.PENDING;

    @Column(name = "created_at",     nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "retry_count",    nullable = false)
    private int retryCount = 0;

    public static OutboxEvent pending(String eventType, String aggregateType,
                                      String aggregateId, String correlationId,
                                      String jsonPayload) {
        OutboxEvent e = new OutboxEvent();
        e.setEventType(eventType);
        e.setAggregateType(aggregateType);
        e.setAggregateId(aggregateId);
        e.setCorrelationId(correlationId);
        e.setPayload(jsonPayload);
        return e;
    }
}
