# Slice 6 – Scheduling Service – Kafka Consumer Skeleton

## Module

`backend/scheduling-service` – Spring Boot 3, port **8082**

## What this slice does

- Boots as a standalone Spring Boot service
- Configures a Kafka consumer with `group-id=scheduling-service`, manual offset commit
- `RegimenCreatedListener` subscribes to `aethertrack.regimen.created`
- Logs all envelope fields + payload summary on receipt
- Acknowledges offset on success; leaves unacknowledged on error (triggers redelivery)

## Consumer configuration

| Setting | Value |
|---------|-------|
| group-id | `scheduling-service` |
| auto-offset-reset | `earliest` |
| enable-auto-commit | `false` |
| ack-mode | `MANUAL_IMMEDIATE` |
| concurrency | 1 |
| max-poll-records | 50 |

## How to run locally

```bash
cd infra && docker compose up -d kafka

cd backend
./mvnw -pl supplement-domain-service spring-boot:run &   # starts outbox poller
./mvnw -pl scheduling-service spring-boot:run

# POST a regimen -> domain service -> outbox -> Kafka -> scheduling-service logs it
```

## Health check

```bash
curl http://localhost:8082/actuator/health | jq .status
```

## Run tests

```bash
cd backend
./mvnw -pl scheduling-service test
# Uses EmbeddedKafka; no external broker needed
```

## Next slice

Slice 7 – Timefold domain model (SupplementDose, TimeSlot, constraint configuration)
