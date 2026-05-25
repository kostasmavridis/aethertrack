# Slice 7 – Timefold Domain Model Skeleton (Pure Java)

## Goal

A standalone Timefold planning model and constraint set inside `scheduling-service`,
with a smoke test that runs the solver without any DB or Kafka wiring.

## Timefold version

`timefold-solver-bom:1.17.0` added to `scheduling-service/pom.xml`.
Adds `timefold-solver-spring-boot-starter` for Spring autoconfiguration
and `timefold-solver-test` for `ConstraintVerifier` unit tests.

## Domain model

### `TimeSlot` (problem fact / planning value)

Immutable class representing a discrete intake window.

| Field | Type | Example |
|-------|------|---------|
| id | String | `"MORNING"` |
| startTime | LocalTime | `07:00` |
| endTime | LocalTime | `09:00` |
| withMeal | boolean | `true` |
| nightTime | boolean | `false` |

`TimeSlot.defaultSlots()` returns 5 standard windows:
`MORNING`, `MIDDAY`, `AFTERNOON`, `EVENING`, `NIGHT`.

### `SupplementDose` (`@PlanningEntity`)

| Field | Notes |
|-------|-------|
| `supplementCode` | e.g. `"MAG-GLY-400"` |
| `nightTimeRequired` | Hard constraint: must land in `nightTime=true` slot |
| `mealRequired` | Hard constraint: must land in `withMeal=true` slot |
| `assignedSlot` | `@PlanningVariable` – value Timefold assigns |

### `SupplementSchedule` (`@PlanningSolution`)

- `@ValueRangeProvider(id="timeSlotRange")` → `List<TimeSlot>`
- `@PlanningEntityCollectionProperty` → `List<SupplementDose>`
- `@PlanningScore` → `HardSoftScore`

## Constraints

| ID | Type | Rule |
|----|------|------|
| H1 | HARD | No two doses of the same supplement in the same slot |
| H2 | HARD | Night-time required dose must be in a `nightTime=true` slot |
| H3 | HARD | Meal-required dose must be in a `withMeal=true` slot |
| S1 | SOFT | Minimise number of distinct used slots (1 penalty per slot used) |
| S2 | SOFT | Prefer earlier slots for non-night supplements (weight = start hour) |

## Running the tests

```bash
cd backend

# ConstraintVerifier unit tests (instant, no solver run)
./mvnw -pl scheduling-service test -Dtest=SupplementScheduleConstraintProviderTest

# Solver smoke test (2-second real solve, prints schedule to stdout)
./mvnw -pl scheduling-service test -Dtest=SolverSmokeTest
```

Expected output from SolverSmokeTest:
```
=== Solver Smoke Test Result ===
  VIT-D3       -> MORNING(07:00-09:00)
  VIT-C        -> MORNING(07:00-09:00)
  MAG-GLY      -> NIGHT(21:00-23:00)    <- nightTimeRequired satisfied
  FISH-OIL     -> MORNING(07:00-09:00)  <- mealRequired satisfied
  Score: 0hard/-Xsoft
```

## Configuration

```yaml
timefold:
  solver:
    termination:
      spent-limit: 10s
    environment-mode: REPRODUCIBLE
```

## Next slice

Slice 8 – Wire `RegimenCreatedListener` → `SupplementSchedule` → Timefold solver
(map Kafka payload to planning problem, run solver, log result).
