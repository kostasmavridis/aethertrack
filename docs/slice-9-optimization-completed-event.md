# Slice 9 – OptimizationCompleted Event & Persistence

## Goal

After Timefold solves a regimen, persist the schedule and reliably emit
an `OptimizationCompleted` domain event to Kafka via the transactional outbox pattern.

## New files

| File | Purpose |
|------|------|
| `V2__add_scheduled_dose_and_outbox.sql` | `scheduled_dose` + `outbox_event` tables |
| `domain/ScheduledDose` | JPA entity: one row per dose assignment |
| `repository/ScheduledDoseRepository` | `findByRegimenId`, `deleteByRegimenId` |
| `events/OptimizationCompletedPayload` | Record: score + `List<DoseAssignment>` |
| `outbox/OutboxEvent` | JPA entity for outbox row |
| `outbox/OutboxStatus` | `PENDING` / `SENT` / `FAILED` constants |
| `outbox/OutboxEventRepository` | `findPendingEvents`, `markAs` |
| `outbox/OutboxRelayService` | `@Scheduled` → `kafkaTemplate.send().get()` → mark SENT |
| `service/SchedulePersistenceService` | Atomic: delete + insert doses + write outbox |
| `service/SchedulingService` | Updated: calls `persistenceService.persistAndEnqueue()` |
| `listener/RegimenCreatedListener` | Updated: passes `correlationId` to `scheduleRegimen()` |
| `SchedulingServiceApplication` | Added `@EnableScheduling` |
| `application.yml` | JPA config, Kafka producer, `outbox.poll-interval-ms` |

## Transaction boundary

```
BEGIN
  DELETE scheduling.scheduled_dose WHERE regimen_id = ?
  INSERT scheduling.scheduled_dose  (N rows)
  INSERT scheduling.outbox_event    (status = PENDING)
COMMIT

--- 2 s later (OutboxRelayService) ---
SELECT * FROM scheduling.outbox_event WHERE status = 'PENDING'
  kafkaTemplate.send("optimization.completed", regimenId, payload).get()
  UPDATE outbox_event SET status = 'SENT'
```

## `scheduled_dose` schema

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGSERIAL PK | |
| `regimen_id` | BIGINT | indexed |
| `regimen_item_id` | BIGINT | FK → `public.regimen_item` |
| `timeslot` | VARCHAR(32) | e.g. `MORNING` |
| `timeslot_start` | TIME | e.g. `07:00` |
| `timeslot_end` | TIME | e.g. `09:00` |
| `day_offset` | INT | 0 = today |
| `hard_score` | INT | copied from solver |
| `soft_score` | INT | copied from solver |

## `OptimizationCompleted` payload example

```json
{
  "regimenId": 42,
  "patientId": "p-001",
  "hardScore": 0,
  "softScore": -3,
  "assignments": [
    { "regimenItemId": 10, "supplementCode": "VIT-D3",
      "timeslot": "MORNING", "timeslotStart": "07:00", "timeslotEnd": "09:00", "dayOffset": 0 },
    { "regimenItemId": 11, "supplementCode": "MAG-GLY",
      "timeslot": "NIGHT",   "timeslotStart": "21:00", "timeslotEnd": "23:00", "dayOffset": 0 }
  ]
}
```

Consumers:
- **fhir-service** (Slice 12): updates `NutritionOrder` Timing fields
- **frontend** (Slice 16): renders schedule timeline + score breakdown

## Running tests

```bash
cd backend
./mvnw -pl scheduling-service test \
  -Dtest=SchedulePersistenceServiceTest,OutboxRelayServiceTest
```

## Next slice

Slice 10 – `fhir-service` skeleton: HAPI FHIR R5 client, connectivity health check.
