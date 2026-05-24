# Architecture Overview

See `deep-research-report.md` for the full design rationale.

## Services

- **supplement-domain-service** – manages supplement catalog, regimens and rules.  Root event producer.
- **scheduling-service** – consumes `RegimenCreated`; runs Timefold solver; publishes `OptimizationCompleted`.
- **fhir-service** – maps domain events to HAPI FHIR R5 resources (NutritionOrder, NutritionIntake).
- **intake-service** – records user intake logs; emits `IntakeLogged` and evaluates adherence.

## Event flow (happy path)

```
UI  ──POST /api/regimens──►  domain-service  ──RegimenCreated──►  scheduling-service
                                                                         │
                                              ◄──OptimizationCompleted──┘
                                                         │
                                                   fhir-service  (updates NutritionOrder timing)

UI  ──POST /api/intake──►  intake-service  ──IntakeLogged──►  fhir-service (NutritionIntake)
                                                   │
                                             AdherenceEvaluated
```
