# Slice 12 – OptimizationCompleted → Update Timing

## Goal

Consume `optimization.completed` events in `fhir-service`, translate solver-assigned
time slots into FHIR `Timing.when` values, update the existing `NutritionOrder` on the
HAPI server, and emit `fhir.sync.completed` / `fhir.sync.failed` domain events via an
outbox relay.

## New/updated files

| File | Purpose |
|------|---------|
| `OptimizationCompletedPayload` | Event payload for solved schedules |
| `FhirSyncCompletedPayload` | Success event payload |
| `FhirSyncFailedPayload` | Failure event payload |
| `OptimizationCompletedListener` | Consumes `optimization.completed` with manual ACK |
| `NutritionOrderTimingSyncService` | Reads existing `NutritionOrder`, updates schedule timing, saves sync outbox event |
| `FhirSyncOutboxEvent` | Outbox entity for result events |
| `FhirSyncOutboxRepository` | Read pending outbox rows |
| `FhirSyncOutboxRelayService` | Scheduled relay to Kafka; marks SENT/FAILED |
| `KafkaConfig` | Producer beans added alongside manual-ACK consumer config |
| `V1__create_fhir_schema.sql` | Extended with `fhir_sync_outbox` table |
| `FhirServiceApplication` | `@EnableScheduling` for outbox relay |
| `NutritionOrderTimingSyncServiceTest` | Verifies timing update + completed outbox |
| `FhirSyncOutboxRelayServiceTest` | Verifies relay marks event SENT |

## Data flow

```text
optimization.completed
   -> OptimizationCompletedListener
   -> NutritionOrderTimingSyncService.syncTiming()
      -> lookup regimen_fhir_mapping
      -> read NutritionOrder from HAPI
      -> map timeslot codes to Timing.when
      -> update NutritionOrder on HAPI
      -> save fhir.sync.completed in outbox
   -> ACK

on failure
   -> save fhir.sync.failed in outbox
   -> throw for Kafka retry / backoff

scheduled relay
   -> send pending outbox rows to Kafka
   -> mark SENT or FAILED after 5 attempts
```

## timeslotCode → FHIR Timing.when

| timeslotCode | FHIR EventTiming |
|-------------|------------------|
| `MORNING`, `AM`, `BREAKFAST` | `MORN` |
| `MIDDAY`, `NOON`, `LUNCH` | `NOON` |
| `EVENING`, `PM`, `DINNER` | `EVE` |
| `NIGHT`, `BEDTIME` | `HS` |
| `WITH_MEAL`, `MEAL` | `PCM` |

## Notes

- This slice updates all `NutritionOrder.supplement[*].schedule[0].timing.repeat.when` values
  using the distinct time windows found in the optimization payload.
- It is intentionally minimal: assignments are not yet matched one-by-one to specific supplements;
  Slice 12 keeps every supplement aligned to the optimized daily windows while preserving a small implementation.
- Success/failure notifications are decoupled from Kafka publishing via the outbox table to avoid losing sync status on process crash.

## Run tests

```bash
cd backend
./mvnw -pl fhir-service test \
  -Dtest=NutritionOrderTimingSyncServiceTest,FhirSyncOutboxRelayServiceTest
```
