# Slice 14 – Adherence Evaluation Logic

## Goal

Consume `IntakeLogged` events inside `intake-service`, compare the actual
intake timestamp against the scheduled window, compute an outcome
(`ON_TIME` | `EARLY` | `LATE` | `UNSCHEDULED`), persist an `adherence_summary`
row, and enqueue an `AdherenceEvaluated` event for downstream use (e.g. frontend
Slice 17 colour coding, future notifications).

## New/updated files

| File | Purpose |
|------|---------|
| `V2__add_adherence_schema.sql` | `scheduled_dose_ref` + `adherence_summary` tables |
| `domain/ScheduledDoseRef` | Local window reference for each regimen item |
| `domain/AdherenceSummary` | Persisted outcome row |
| `repository/ScheduledDoseRefRepository` | `findFirstByRegimenItemId` |
| `repository/AdherenceSummaryRepository` | `findByPatientIdOrderByEvaluatedAtDesc` |
| `events/AdherenceEvaluatedPayload` | Event payload for adherence results |
| `events/KafkaTopics` | Added `adherence.evaluated` topic constant |
| `service/AdherenceEvaluationService` | Core evaluation logic + outbox enqueue |
| `listener/IntakeLoggedListener` | Manual-ACK Kafka consumer → evaluation |
| `config/KafkaConfig` | Added consumer factory alongside existing producer |
| `api/AdherenceController` | `GET /api/adherence?patientId=` |
| `application.yml` | Updated consumer group-id, Kafka consumer config |
| `AdherenceEvaluationServiceTest` | 5 tests: ON_TIME, LATE, EARLY, UNSCHEDULED, outbox |

## Outcome logic

```text
Given intake at local time T, scheduled window [start, end]:

  start ≤ T ≤ end  →  ON_TIME   (deviationMins = 0)
  T < start         →  EARLY     (deviationMins = negative)
  T > end           →  LATE      (deviationMins = positive)
  no ref found      →  UNSCHEDULED
```

All comparisons are in UTC local-time (no DST ambiguity; supplement windows are
daily schedule slots, not wall-clock events).

## Transaction boundary

```text
Kafka: intake.logged
   -> IntakeLoggedListener
   -> AdherenceEvaluationService.evaluate()
      -> findFirstByRegimenItemId()          [SELECT]
      -> summaryRepo.save(AdherenceSummary)  [INSERT]
      -> outboxRepo.save(OutboxEvent)        [INSERT adherence.evaluated, PENDING]
   -> ACK

Scheduled relay (OutboxRelayService, every 2s)
   -> KafkaTemplate.send(adherence.evaluated)
   -> mark SENT
```

## Seeding scheduled_dose_ref

For local smoke tests, insert a reference row directly:

```sql
INSERT INTO intake.scheduled_dose_ref
    (regimen_item_id, patient_id, timeslot_code, window_start_time, window_end_time)
VALUES
    (101, 'patient-1', 'MORNING', '08:00', '09:00');
```

In a later slice this table will be populated by consuming `OptimizationCompleted`
events from the scheduling-service.

## Run tests

```bash
cd backend
./mvnw -pl intake-service test -Dtest=AdherenceEvaluationServiceTest
```
