# AetherTrack

> Intelligent, FHIR-aligned supplement scheduling platform.

## Monorepo layout

```
aethertrack/
├── backend/                        # Java 21 + Spring Boot 4 multi-module Maven project
│   ├── supplement-domain-service/  # Core domain: supplements, regimens, rules
│   ├── scheduling-service/         # Timefold-based optimisation engine
│   ├── fhir-service/               # HAPI FHIR R5 bridge
│   └── intake-service/             # Intake logging & adherence evaluation
├── frontend/                       # React 19 + TypeScript + Vite SPA
├── infra/                          # Docker Compose, configs, nginx
└── docs/                           # Architecture decisions, API specs
```

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21 |
| Maven | 3.9+ |
| Node | 22 LTS |
| pnpm | 9+ |
| Docker | 27+ |
| Docker Compose | v2 plugin |

## Quick start (local dev)

```bash
# 1. Start infrastructure (Postgres, Kafka, HAPI FHIR)
cd infra && docker compose up -d

# 2. Build all backend modules
cd ../backend && ./mvnw clean install -DskipTests

# 3. Run domain service
cd supplement-domain-service && ../mvnw spring-boot:run

# 4. Start frontend
cd ../../frontend && pnpm install && pnpm dev
```

## Slice roadmap

See [docs/slice-plan.md](docs/slice-plan.md) for the full implementation slice plan.
