# Slice 19 – Docker Compose Full Topology

## Goal

Extend the minimal Compose stack from Slice 18 to bring up every service and
the frontend together: `scheduling-service`, `fhir-service`, `intake-service`,
and the React `frontend` (served by nginx). All services share one bridge
network and start in dependency order via `depends_on: condition: service_healthy`.

## Files added / changed

| Path | Purpose |
|------|---------|
| `infra/docker-compose.yml` | All five application services live (no more commented-out blocks) |
| `backend/scheduling-service/Dockerfile` | Multi-stage Maven build → JRE 21 image, port 8082 |
| `backend/fhir-service/Dockerfile` | Multi-stage Maven build → JRE 21 image, port 8083 |
| `backend/intake-service/Dockerfile` | Multi-stage Maven build → JRE 21 image, port 8084 |
| `frontend/Dockerfile` | `node:22-alpine` build → `nginx:alpine` runtime, port 80 |
| `frontend/nginx.conf` | SPA fallback + `/api/` reverse-proxy to `supplement-domain-service:8081` |

## Services & ports

| Service | Image | Default port | Health check |
|---------|-------|--------------|--------------|
| `postgres` | `postgres:18-alpine` | 5432 | `pg_isready` |
| `kafka` | `redpandadata/redpanda:v25.3.9` | 19092 / 9092 | `rpk cluster health` |
| `kafka-console` | `redpandadata/console:v2.8.4` | 8085 | — |
| `hapi-fhir` | `hapiproject/hapi:latest` | 8080 | `curl /fhir/metadata` |
| `supplement-domain-service` | local build | 8081 | `curl /actuator/health` |
| `scheduling-service` | local build | 8082 | `curl /actuator/health` |
| `fhir-service` | local build | 8083 | `curl /actuator/health` |
| `intake-service` | local build | 8084 | `curl /actuator/health` |
| `frontend` | local build | 5173 → :80 | `curl /healthz` |

## Health-check & startup chain

```text
postgres (pg_isready)
  └─> hapi-fhir          (curl /fhir/metadata)  
  └─> supplement-domain-service (curl /actuator/health)
        │  also waits on: kafka (rpk cluster health)
        ├─> scheduling-service  (curl /actuator/health)
        ├─> fhir-service        (curl /actuator/health)  ← also waits on hapi-fhir
        └─> frontend            (curl /healthz)

intake-service waits on: postgres + kafka directly
```

## Environment variable mapping

Each service reads env vars with the prefixes declared in its `application.yml`:

| Service | DB env vars | Kafka env var |
|---------|-------------|---------------|
| `supplement-domain-service` | `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | `SPRING_KAFKA_BOOTSTRAP_SERVERS` |
| `scheduling-service` | `DATASOURCE_URL/USERNAME/PASSWORD` | `KAFKA_BOOTSTRAP_SERVERS` |
| `fhir-service` | `DATASOURCE_URL/USERNAME/PASSWORD` | `KAFKA_BOOTSTRAP_SERVERS` |
| `intake-service` | `DATASOURCE_URL/USERNAME/PASSWORD` | `KAFKA_BOOTSTRAP_SERVERS` |

`fhir-service` additionally receives `FHIR_BASE_URL=http://hapi-fhir:8080/fhir`.

## Dockerfile design (all backend services)

- **Stage 1** `eclipse-temurin:21-jdk-alpine`: installs Maven via `apk`, copies all
  module POMs for layer caching, then builds only the target module with
  `-pl <module> -am -DskipTests`.
- **Stage 2** `eclipse-temurin:21-jre-alpine`: minimal runtime, copies the fat JAR.
- JVM flags: `UseContainerSupport` + `MaxRAMPercentage=75`.

## Frontend Dockerfile design

- **Stage 1** `node:22-alpine`: `npm ci` + `npm run build`. `VITE_API_BASE_URL` is
  passed as a build-arg (defaults to `/api`) so the SPA never embeds a
  hard-coded hostname.
- **Stage 2** `nginx:alpine`: serves `/usr/share/nginx/html` with SPA fallback
  (`try_files $uri /index.html`) and proxies `/api/` to
  `supplement-domain-service:8081`.

## Quick start

```bash
cd infra
cp .env.example .env          # adjust passwords / ports if needed

# Build all images
docker compose build

# Start full stack
docker compose up -d

# Follow logs
docker compose logs -f

# Check all containers are healthy
docker compose ps
```

## Verify – end-to-end event flow

```bash
# 1. Open the React UI
open http://localhost:5173

# 2. Or create a regimen via curl
curl -s -X POST http://localhost:8081/api/regimens \
  -H 'Content-Type: application/json' \
  -d '{
    "patientId": "patient-1",
    "name": "Morning Stack",
    "items": [{
      "supplementId": 1,
      "supplementCode": "VIT-D3",
      "doseQty": 2000,
      "doseUnit": "IU",
      "frequencyPerDay": 1,
      "scheduleWindow": "MORNING"
    }]
  }' | jq .regimenId

# 3. Watch scheduling-service solve and emit OptimizationCompleted
docker compose logs -f scheduling-service

# 4. Confirm scheduled_dose rows were written (scheduling schema)
docker exec aethertrack-postgres psql -U aethertrack -d aethertrack \
  -c "SELECT * FROM scheduling.scheduled_dose LIMIT 10;"

# 5. Confirm FHIR NutritionOrder was created
curl -s http://localhost:8080/fhir/NutritionOrder | jq '.total'
# → 1

# 6. Log an intake and watch adherence evaluation
curl -s -X POST http://localhost:8084/api/intake \
  -H 'Content-Type: application/json' \
  -d '{"patientId":"patient-1","regimenItemId":1,"takenAt":"2026-05-25T07:00:00Z","quantity":1}'

docker compose logs -f intake-service
# → AdherenceEvaluationService: outcome=ON_TIME
```

## Schemas pre-created at DB init

`infra/postgres/init/01-schemas.sql` (added in Slice 18) creates the
`supplement`, `scheduling`, `fhir`, and `intake` schemas at container first
start. Each service's Flyway runs migrations inside its own schema on boot.

## Next

Slice 20 – Security: OAuth2/OpenID Connect with Keycloak, Spring Security
resource server, and frontend bearer-token flow.
