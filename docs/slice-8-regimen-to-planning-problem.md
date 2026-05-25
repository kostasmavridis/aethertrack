# Slice 8 – Map RegimenCreated → Planning Problem

## Goal

When a `RegimenCreated` Kafka event is received, fetch regimen items from Postgres,
map them to a Timefold `SupplementSchedule`, run the solver, and log the result.
No persistence or outbound events yet (Slice 9 adds those).

## New files

| File | Purpose |
|------|---------|
| `db/migration/V1__create_scheduling_read_schema.sql` | `scheduling` schema + read views over `public.regimen_item` |
| `repository/RegimenItemReadModel.java` | Record projection from view |
| `repository/RegimenItemRepository.java` | `JdbcClient` `findByRegimenId` |
| `solver/PlanningProblemMapper.java` | DB / payload → `SupplementSchedule` |
| `service/SchedulingService.java` | Fetch → Map → Solve → Log |
| `listener/RegimenCreatedListener.java` | `@KafkaListener` with MDC + manual ACK |
| `events/DomainEvent.java` | Updated: added `correlationId`, `causationId` |
| `config/KafkaConfig.java` | Manual ACK, exponential back-off, DLT |
| `application.yml` | Datasource, Flyway, topic name |

## Data flow

```
Kafka: regimen.created
    │
    ► RegimenCreatedListener → MDC correlationId
    │
    ► SchedulingService.scheduleRegimen()
    ├─► RegimenItemRepository.findByRegimenId()  [SQL on v_regimen_item]
    ├─► PlanningProblemMapper.fromReadModels()    [freq expansion]
    └─► SolverManager.solveAndListen()            [10s]
           └─► logSchedule()                        [box-drawing log]
```

## frequencyPerDay expansion

`frequencyPerDay = 2` → 2 separate `SupplementDose` entities so Timefold
assigns each to a different slot.

## Fallback strategy

| `scheduleWindow` | `nightTimeRequired` | `mealRequired` |
|------------------|---------------------|----------------|
| `"NIGHT"` | true | false |
| `"MEAL"` / `"WITH_MEAL"` | false | true |
| null / other | false | false |

## Error handling

| Scenario | Behaviour |
|----------|-----------|
| DB unavailable | Falls back to payload; logs WARN |
| Solver throws | Logs ERROR; offset not ACK'd → retry |
| 5 failures | Routed to `regimen.created.DLT` |

## Running tests

```bash
cd backend
./mvnw -pl scheduling-service test \
  -Dtest=SchedulingServiceIntegrationTest,PlanningProblemMapperTest
```

## Manual smoke (infra running)

```bash
cd infra && docker compose up -d postgres kafka
cd backend && ./mvnw -pl scheduling-service spring-boot:run &

docker exec aethertrack-kafka rpk topic produce regimen.created \
  --brokers localhost:9092 <<'EOF'
{"eventId":"e1","eventType":"RegimenCreated","version":"1","timestamp":"2026-05-25T00:00:00Z","correlationId":"c1","payload":{"regimenId":999,"patientId":"p-test","name":"Smoke","items":[{"itemId":1,"supplementId":10,"supplementCode":"VIT-D3","doseQty":1000,"doseUnit":"IU","frequencyPerDay":1,"scheduleWindow":null},{"itemId":2,"supplementId":11,"supplementCode":"MAG-GLY","doseQty":400,"doseUnit":"mg","frequencyPerDay":1,"scheduleWindow":"NIGHT"}]}}
EOF
```

## Next slice

Slice 9 – persist solved schedule into `scheduled_dose` table and emit
`OptimizationCompleted` via transactional outbox.
