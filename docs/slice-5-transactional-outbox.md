# Slice 5 – Transactional Outbox Pattern

## Why outbox?

Without the outbox, a crash between DB commit and Kafka send loses the event silently.
The outbox pattern writes the event row in the **same DB transaction** as the domain row,
then a background poller relays it to Kafka. This gives **at-least-once delivery**
with no distributed coordination needed.

## Flow

```
POST /api/regimens
      |
      +-- BEGIN TX
      |     regimen INSERT
      |     regimen_item INSERT
      |     outbox_event INSERT (status=PENDING)
      +-- COMMIT TX
      |
      HTTP 201 returned immediately
      |
OutboxPoller (every 5s)
      |
      +-- SELECT pending rows (batch 50)
      |     for each row:
      |       kafkaTemplate.send(...).get()  <- synchronous within poll cycle
      |       UPDATE status = SENT
```

## Schema

| Column | Type | Notes |
|--------|------|-------|
| id | UUID PK | gen_random_uuid() |
| aggregate_type | VARCHAR | e.g. "Regimen" |
| aggregate_id | VARCHAR | e.g. "42" |
| event_type | VARCHAR | e.g. "RegimenCreated" |
| version | VARCHAR | default "v1" |
| correlation_id | VARCHAR | from X-Correlation-ID |
| payload | JSONB | serialized event payload |
| topic | VARCHAR | Kafka topic |
| status | VARCHAR | PENDING / SENT / FAILED |
| created_at | TIMESTAMPTZ | |
| processed_at | TIMESTAMPTZ | set on SENT |
| retry_count | INT | incremented on failure |
| last_error | TEXT | last exception message |

## Delivery guarantees

- **At-least-once**: poller retries any row that stays PENDING after a crash
- **Ordering**: rows keyed by aggregateId → same Kafka partition per regimen
- **Idempotent consumers**: downstream services must handle duplicates via eventId dedup

## Verifying locally

```bash
cd infra && docker compose up -d postgres kafka
cd backend && ./mvnw -pl supplement-domain-service spring-boot:run

# POST a regimen, then watch the outbox table
SUPP_ID=$(curl -s http://localhost:8081/api/supplements | jq '.[0].id')
curl -s -X POST http://localhost:8081/api/regimens \
  -H 'Content-Type: application/json' \
  -d "{\"patientId\":\"p1\",\"name\":\"stack\",\"items\":[{\"supplementId\":$SUPP_ID,\"doseQty\":1,\"doseUnit\":\"tablet\",\"frequencyPerDay\":1}]}"

# Row appears PENDING, then SENT within 5 seconds
docker exec -it $(docker ps -qf name=postgres) \
  psql -U aethertrack -c "SELECT id, event_type, status, retry_count, processed_at FROM outbox_event ORDER BY created_at;"
```

## Run tests

```bash
cd backend
./mvnw -pl supplement-domain-service test -Dtest=OutboxServiceTest,OutboxPollerIT
```

## Next
Slice 6 – Scheduling Service skeleton with Kafka consumer for RegimenCreated
