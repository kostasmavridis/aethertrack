# Slice 4 – Kafka Event Contracts & Config

## Event model

All domain events use the generic `DomainEvent<P>` envelope:

```json
{
  "eventId":       "550e8400-e29b-41d4-a716-446655440000",
  "eventType":     "RegimenCreated",
  "version":       "v1",
  "correlationId": "req-abc-123",
  "occurredAt":    "2026-05-25T01:00:00Z",
  "payload": {
    "regimenId":  42,
    "patientId":  "patient-123",
    "name":       "Morning stack",
    "items": [
      {
        "itemId":          100,
        "supplementId":    1,
        "supplementCode":  "VIT-D3-1000",
        "doseQty":         1.0,
        "doseUnit":        "tablet",
        "frequencyPerDay": 1,
        "scheduleWindow":  "MORNING"
      }
    ]
  }
}
```

## Topic

`aethertrack.regimen.created`

- **Key**: `regimenId` (string) — ordered delivery per regimen within a partition
- **Producer acks**: `all`, idempotent (`enable.idempotence=true`), `retries=3`

## All planned topics (KafkaTopics.java)

| Constant | Topic name |
|----------|------------|
| `REGIMEN_CREATED` | `aethertrack.regimen.created` |
| `REGIMEN_UPDATED` | `aethertrack.regimen.updated` |
| `OPTIMIZATION_DONE` | `aethertrack.optimization.completed` |
| `INTAKE_LOGGED` | `aethertrack.intake.logged` |
| `ADHERENCE_EVALUATED` | `aethertrack.adherence.evaluated` |
| `FHIR_SYNC_DONE` | `aethertrack.fhir.sync.completed` |

## Correlation ID flow

Every HTTP request through `CorrelationIdFilter` gets a correlation ID
(from `X-Correlation-ID` header or a fresh UUID), stored in `CorrelationIdHolder`
(thread-local), and injected into every event envelope automatically.

## Running locally with Redpanda

```bash
cd infra && docker compose up -d kafka
docker exec -it $(docker ps -qf name=redpanda) rpk topic create aethertrack.regimen.created --partitions 3
docker exec -it $(docker ps -qf name=redpanda) rpk topic consume aethertrack.regimen.created
```

Then POST a regimen — the JSON event appears in the consumer output.

## Note on reliability

This slice uses fire-and-forget publishing with a completion callback for logging.
Slice 5 replaces this with the **Transactional Outbox Pattern** for guaranteed delivery.

## Unit tests

```bash
cd backend
./mvnw -pl supplement-domain-service test -Dtest=RegimenEventPublisherTest
```
