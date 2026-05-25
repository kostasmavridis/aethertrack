# Slice 11 – RegimenCreated → NutritionOrder

## Goal

Consume `RegimenCreated` events in `fhir-service`, map each regimen to a FHIR
`NutritionOrder` R5 resource, write it to the HAPI server, and persist the
`regimen_id ↔ NutritionOrder logical-ID` mapping in Postgres.

## New files

| File | Purpose |
|------|---------|
| `db/migration/V1__create_fhir_schema.sql` | `fhir` schema + `regimen_fhir_mapping` table |
| `events/DomainEvent` | Generic envelope record |
| `events/RegimenCreatedPayload` | Payload record (must stay in sync with publisher) |
| `events/KafkaTopics` | Topic name constants |
| `config/KafkaConfig` | Manual-ACK consumer factory, exponential back-off, DLT |
| `domain/RegimenFhirMapping` | JPA entity: `regimen_id` ↔ `nutrition_order_id` |
| `repository/RegimenFhirMappingRepository` | `findByRegimenId`, `existsByRegimenId` |
| `mapper/NutritionOrderMapper` | Payload → R5 `NutritionOrder` with Timing derivation |
| `service/NutritionOrderService` | Idempotent: check → map → HAPI create → persist |
| `listener/RegimenCreatedListener` | `@KafkaListener` with MDC + manual ACK |
| `application.yml` | Updated: datasource, Flyway, JPA, Kafka producer |
| `NutritionOrderMapperTest` | 5 unit tests: subject ref, item count, timing codes, frequency |
| `NutritionOrderServiceTest` | 3 tests: create path, idempotency skip, HAPI error propagation |

## Data flow

```
Kafka: regimen.created
    │
    ► RegimenCreatedListener  (MDC correlationId, manual ACK)
    │
    ► NutritionOrderService.createFromRegimen()
    ├─ existsByRegimenId()?  → YES → return (idempotent)
    ├─ NutritionOrderMapper.toNutritionOrder(payload)
    ├─ fhirClient.create().resource(no).execute()  → HAPI R5
    └─ mappingRepo.save(regimenId ↔ fhirId)         → Postgres
```

## scheduleWindow → FHIR Timing.when mapping

| scheduleWindow | FHIR EventTiming |
|----------------|------------------|
| `MORNING`      | `MORN`           |
| `MIDDAY`       | `NOON`           |
| `EVENING`      | `EVE`            |
| `NIGHT`        | `HS` (hour of sleep) |
| `WITH_MEAL` / `MEAL` | `PCM`      |
| `null` / other | (unset; filled by Slice 12) |

## Idempotency

`existsByRegimenId()` is checked before any HAPI call.
If true, the method returns immediately — safe for Kafka re-deliveries
and consumer restarts.

## regimen_fhir_mapping table

| Column | Type | Notes |
|--------|------|-------|
| `regimen_id` | BIGINT UNIQUE | source-of-truth key |
| `nutrition_order_id` | VARCHAR(64) | FHIR logical ID from HAPI |
| `nutrition_order_url` | VARCHAR(256) | full URL |
| `fhir_version` | VARCHAR(16) | default `5.0.0` |

## Running tests

```bash
cd backend
./mvnw -pl fhir-service test \
  -Dtest=NutritionOrderMapperTest,NutritionOrderServiceTest
```

## Smoke test (infra running)

```bash
cd infra && docker compose up -d postgres kafka hapi-fhir
./mvnw -pl fhir-service spring-boot:run &

docker exec aethertrack-kafka rpk topic produce regimen.created \
  --brokers localhost:9092 <<'EOF'
{"eventId":"e1","eventType":"RegimenCreated","version":"1","timestamp":"2026-05-25T00:00:00Z",
 "correlationId":"c1","payload":{"regimenId":1,"patientId":"p-001","name":"Test",
  "items":[{"itemId":1,"supplementId":10,"supplementCode":"VIT-D3",
            "doseQty":1000,"doseUnit":"IU","frequencyPerDay":1,"scheduleWindow":"MORNING"}]}}
EOF

# Verify NutritionOrder created on HAPI
curl -s http://localhost:8080/fhir/NutritionOrder | jq '.total'
# Verify mapping row in Postgres
psql -U aethertrack -d aethertrack \
  -c "SELECT * FROM fhir.regimen_fhir_mapping;"
```

## Next slice

Slice 12 – consume `OptimizationCompleted` events and update the `NutritionOrder`
Timing fields with the actual solver-assigned time slots.
