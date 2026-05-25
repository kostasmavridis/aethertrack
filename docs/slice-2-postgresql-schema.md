# Slice 2 – PostgreSQL Integration & Schema Skeleton

## What's in this slice

| Artifact | Purpose |
|----------|----------|
| `V1__init_schema.sql` | Creates `supplement`, `regimen`, `regimen_item` tables with indexes |
| `V2__seed_supplements.sql` | Seeds 8 common supplements for dev/demo |
| `application.yml` | Full datasource/JPA/Flyway/Actuator config |
| `Supplement.java` | JPA entity with JSONB `nutrients` column |
| `SupplementRepository.java` | Spring Data JPA repository |
| `SupplementDto.java` | Outbound record DTO |
| `SupplementService.java` | Transactional service layer |
| `SupplementController.java` | `GET /api/supplements[?category=X]` |
| `SupplementControllerIT.java` | Testcontainers integration test |

## How to run locally

```bash
# 1. Start Postgres
cd infra && docker compose up -d postgres

# 2. Run the service (Flyway runs migrations automatically on startup)
cd backend && ./mvnw -pl supplement-domain-service spring-boot:run

# 3. Verify
curl http://localhost:8081/actuator/health | jq .
curl http://localhost:8081/api/supplements | jq '.[].name'
curl 'http://localhost:8081/api/supplements?category=MINERAL' | jq length
```

## Schema overview

```
supplement          regimen             regimen_item
──────────          ───────             ────────────
id (PK)             id (PK)             id (PK)
code (UQ)           patient_id          regimen_id (FK→regimen)
name                name                supplement_id (FK→supplement)
category            status              dose_qty / dose_unit
description         care_plan_id        frequency_per_day
nutrients (JSONB)   created_at          schedule_window
active              updated_at          nutrition_order_id
created_at                              created_at / updated_at
updated_at
```

## Run integration tests

```bash
cd backend && ./mvnw -pl supplement-domain-service test
# Testcontainers spins up postgres:16-alpine automatically
```

## Next slice
Slice 3 – Regimen Creation API (`POST /api/regimens` with validation + service layer)
