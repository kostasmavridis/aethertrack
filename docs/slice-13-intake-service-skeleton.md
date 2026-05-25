# Slice 13 – Intake Service Skeleton + IntakeLog Persistence

## Goal

Bootable `intake-service` with REST + JPA + Kafka producer support,
a persisted `intake_log` table, and a `POST /api/intake` endpoint that writes an
intake entry and enqueues an `IntakeLogged` event in an outbox.

## New files

| File | Purpose |
|------|---------|
| `IntakeServiceApplication` | Spring Boot entry point + `@EnableScheduling` |
| `domain/IntakeLog` | JPA entity for patient intake entries |
| `domain/OutboxEvent` | Transactional outbox row |
| `repository/IntakeLogRepository` | JPA repository |
| `repository/OutboxEventRepository` | Outbox query `findTop20ByStatusOrderByCreatedAtAsc` |
| `api/IntakeLogRequest` | Validated POST request payload |
| `api/IntakeLogResponse` | API response DTO |
| `api/IntakeController` | `POST /api/intake` endpoint |
| `events/DomainEvent` | Generic event envelope |
| `events/IntakeLoggedPayload` | Event payload |
| `events/KafkaTopics` | `intake.logged` topic constant |
| `config/KafkaConfig` | KafkaTemplate producer beans |
| `service/IntakeLogService` | `@Transactional` save + outbox enqueue |
| `service/OutboxRelayService` | Scheduled relay to Kafka; marks SENT/FAILED |
| `application.yml` | datasource, Flyway, JPA, Kafka, port 8084 |
| `V1__create_intake_schema.sql` | `intake_log` and `outbox_event` tables |
| `IntakeLogServiceTest` | Verifies save + outbox event generation |
| `OutboxRelayServiceTest` | Verifies relay marks SENT |
| `IntakeControllerTest` | Verifies POST /api/intake response |

## Transaction boundary

```text
POST /api/intake
   -> IntakeController
   -> IntakeLogService.create()
      -> INSERT intake.intake_log
      -> INSERT intake.outbox_event (status=PENDING, topic=intake.logged)
   -> 200 RECORDED

Scheduled relay (every 2s)
   -> SELECT PENDING outbox rows
   -> KafkaTemplate.send(...).get()
   -> mark SENT or FAILED after 5 attempts
```

## Request example

```json
{
  "patientId": "patient-1",
  "regimenItemId": 101,
  "takenDateTime": "2026-05-25T04:00:00Z",
  "quantity": 1.000
}
```

## Run tests

```bash
cd backend
./mvnw -pl intake-service test \
  -Dtest=IntakeLogServiceTest,OutboxRelayServiceTest,IntakeControllerTest
```
