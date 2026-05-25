# Slice 18 – Docker Compose Minimal Stack

## Goal

One `docker-compose.yml` that brings up Postgres, Redpanda (Kafka-compatible),
HAPI FHIR R5, and `supplement-domain-service` with health checks and correct
`depends_on` ordering.

## Files added

| Path | Purpose |
|------|---------|
| `infra/docker-compose.yml` | Full Compose definition for minimal stack |
| `infra/postgres/init/01-schemas.sql` | Creates `supplement`, `scheduling`, `fhir`, `intake` schemas at DB init |
| `infra/redpanda/console-config.yaml` | Redpanda Console broker config |
| `infra/.env.example` | Template for local port/credential overrides |
| `backend/supplement-domain-service/Dockerfile` | Multi-stage Maven build → JRE 21 image |

## Services & ports

| Service | Image | Default port |
|---------|-------|--------------|
| `postgres` | `postgres:16-alpine` | 5432 |
| `kafka` | `redpandadata/redpanda:v24.3.1` | 19092 (ext), 9092 (int) |
| `kafka-console` | `redpandadata/console:v2.7.2` | 8085 |
| `hapi-fhir` | `hapiproject/hapi:v8.0.0` | 8080 |
| `supplement-domain-service` | local build | 8081 |

## Health-check chain

```text
postgres (pg_isready)
  └─> hapi-fhir (curl /fhir/metadata)
  └─> supplement-domain-service (curl /actuator/health ⊇ "UP")
        └─> depends on kafka (rpk cluster health) too
```

## Quick start

```bash
cd infra
cp .env.example .env          # adjust passwords if needed

# Build domain service image
docker compose build supplement-domain-service

# Start everything
docker compose up -d postgres kafka hapi-fhir
docker compose up -d supplement-domain-service

# Watch health
docker compose ps
docker compose logs -f supplement-domain-service
```

## Verify

```bash
# 1. Postgres healthy
docker exec aethertrack-postgres pg_isready -U aethertrack -d aethertrack

# 2. Kafka healthy
docker exec aethertrack-kafka rpk cluster health

# 3. HAPI FHIR responding
curl -s http://localhost:8080/fhir/metadata | jq .fhirVersion
# → "5.0.0"

# 4. Domain service up
curl -s http://localhost:8081/actuator/health | jq .status
# → "UP"

# 5. Create a regimen
curl -s -X POST http://localhost:8081/api/regimens \
  -H 'Content-Type: application/json' \
  -d '{
    "patientId": "patient-1",
    "name": "Morning Stack",
    "items": [
      {
        "supplementId": 1,
        "supplementCode": "VIT-D3",
        "doseQty": 2000,
        "doseUnit": "IU",
        "frequencyPerDay": 1,
        "scheduleWindow": "MORNING"
      }
    ]
  }' | jq .regimenId
# → 1  (or next auto-increment value)
```

## Slice 19 note

The remaining service blocks (scheduling-service, fhir-service, intake-service, frontend)
are present in `docker-compose.yml` but commented out.
Uncomment them one at a time in Slice 19.
