# Infrastructure

## Services

| Container | Image | Port | Notes |
|-----------|-------|------|-------|
| `postgres` | postgres:16-alpine | 5432 | Shared DB for all services |
| `kafka` | redpanda:v24.3.4 | 9092 | KRaft – no ZooKeeper required |
| `hapi-fhir` | hapiproject/hapi:v8.8.0 | 8080 | R5 – backed by Postgres |

## Start infra only

```bash
docker compose up -d postgres kafka hapi-fhir
```

## Check health

```bash
docker compose ps
curl http://localhost:8080/fhir/metadata | jq .fhirVersion
```
