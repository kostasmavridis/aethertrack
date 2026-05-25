# Slice 0 – Repo & Monorepo Skeleton

## Goal

A single Git repository with `backend/`, `frontend/`, `infra/`, and `docs/` folders and all basic build tooling in place.

## Structure

```
aethertrack/
├── backend/                  # Maven multi-module parent
│   ├── pom.xml               # Parent POM with BOM imports
│   ├── supplement-domain-service/
│   ├── scheduling-service/
│   ├── fhir-service/
│   └── intake-service/
├── frontend/                 # React 19 + Vite + TypeScript
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── src/
├── infra/                    # Docker Compose + configs
│   └── docker-compose.yml
└── docs/                     # Architecture & slice docs
    ├── architecture.md
    └── slice-plan.md
```

## Backend: Maven multi-module

The parent `pom.xml` manages shared dependency versions via BOMs:

| BOM / Dependency | Version |
|------------------|---------|
| Spring Boot | 3.4.x |
| Spring Cloud Stream | 4.x |
| Timefold Solver | 1.x |
| HAPI FHIR R5 | 7.x |
| Testcontainers | 1.20.x |

All four service modules inherit from the parent and add only their specific dependencies.

### Planned modules

| Module | Port | Purpose |
|--------|------|--------|
| `supplement-domain-service` | 8081 | Supplement & regimen management |
| `scheduling-service` | 8082 | Timefold-powered scheduling |
| `fhir-service` | 8083 | FHIR R5 integration (HAPI) |
| `intake-service` | 8084 | Intake logging & adherence |

## Frontend: React 19 + Vite + TypeScript

- **React 19.1** with functional components and hooks
- **Vite 6** for fast dev server and optimised production builds
- **TypeScript 5.8** with strict mode enabled
- **React Router 7** for client-side routing
- **ESLint** + **Prettier** for code quality
- Vite dev server proxies `/api` → `localhost:8081` (no CORS issues during development)

## Infra: Docker Compose skeleton

Initial compose file brings up infrastructure dependencies only:

> **Note:** PostgreSQL 18 changed the default `PGDATA` path to `/var/lib/postgresql/18/docker`
> and the volume mount target to the parent `/var/lib/postgresql`. The compose file handles this explicitly.

| Service | Image | Port |
|---------|-------|------|
| postgres | `postgres:18-alpine` | 5432 |
| kafka (Redpanda) | `redpandadata/redpanda:v25.3.9` | 9092 |
| hapi-fhir | `hapiproject/hapi:latest` (R5) | 8080 |

Application service blocks are present but commented out, to be uncommented slice by slice from Slice 18 onwards.

## First commands

```bash
# Verify Maven parent resolves
cd backend && ./mvnw validate

# Install frontend deps and start dev server
cd ../frontend && pnpm install && pnpm dev
# → http://localhost:5173

# Start infrastructure
cd ../infra && docker compose up -d postgres kafka hapi-fhir
docker compose ps   # all should be healthy

# Verify HAPI FHIR
curl http://localhost:8080/fhir/metadata | jq .fhirVersion  # → "5.0.0"
```

## Next slice

Slice 1 – Core Domain Service: bootable `supplement-domain-service` with health endpoint.
