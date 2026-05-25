# Slice 1 – Core Domain Service Skeleton

## Goal

A bootable `supplement-domain-service` Spring Boot application with basic plumbing, health endpoint, and dev profile configuration — no domain logic yet.

## Module

`backend/supplement-domain-service` — Spring Boot 3, port **8081**

## Dependencies

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-web` | REST API |
| `spring-boot-starter-data-jpa` | JPA + Hibernate |
| `spring-boot-starter-actuator` | Health + metrics endpoints |
| `spring-boot-starter-validation` | Bean Validation (JSR-380) |
| `spring-kafka` | Kafka producer (wired in Slice 4) |
| `flyway-core` | DB migrations |
| `postgresql` | JDBC driver |
| `lombok` | Boilerplate reduction |
| `spring-boot-starter-test` | JUnit 5 + Mockito |
| `testcontainers` (postgres) | Integration tests |

## Package structure

```
com.aethertrack/
├── config/          # Spring configuration classes
├── domain/          # JPA entities, repositories, services
│   ├── supplement/
│   └── regimen/
├── api/             # REST controllers + DTOs
├── events/          # Event envelopes + payloads
└── outbox/          # Transactional outbox (Slice 5)
```

## Application profiles

| Profile | Datasource | Notes |
|---------|-----------|-------|
| (default) | `localhost:5432/aethertrack` | Local dev with Docker Compose (`postgres:18-alpine`) |
| `test` | Testcontainers Postgres | Integration tests |

## Endpoints added

| Method | Path | Description |
|--------|------|-------------|
| GET | `/actuator/health` | Spring Boot health probe |
| GET | `/actuator/info` | App info |
| GET | `/api/supplements` | Returns seed supplement list (Slice 2+) |

## How to run locally

```bash
# Start Postgres 18
cd infra && docker compose up -d postgres

# Run the service
cd backend
./mvnw -pl supplement-domain-service spring-boot:run

# Verify it's up
curl http://localhost:8081/actuator/health | jq .status
# → "UP"
```

## Smoke test

```bash
./mvnw -pl supplement-domain-service test -Dtest=SupplementDomainServiceApplicationTests
```

The `contextLoads` test boots the full Spring context against a Testcontainers Postgres instance to verify wiring.

## Next slice

Slice 2 – PostgreSQL schema: Flyway migrations, `supplement` and `regimen` tables, seed data.
