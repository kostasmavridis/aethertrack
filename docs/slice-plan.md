# Implementation Slice Plan

Each slice is designed to be completed in a single ~8-minute Perplexity session.

| # | Slice | Key output |
|---|-------|-----------|
| 0 | Repo & monorepo skeleton | This repo |
| 1 | Domain service – empty Spring Boot app | Bootable service |
| 2 | PostgreSQL + schema skeleton | DB migrations |
| 3 | Regimen creation API (sync, no events) | POST /api/regimens |
| 4 | Kafka event contracts (publish only) | RegimenCreated event |
| 5 | Transactional Outbox pattern | Reliable publishing |
| 6 | Scheduling service – skeleton + consumer | Kafka consumer |
| 7 | Timefold model skeleton (pure Java) | Solver smoke test |
| 8 | Map RegimenCreated → planning problem | Real solving |
| 9 | OptimizationCompleted event + persist | Schedule in DB |
| 10 | FHIR service – skeleton + HAPI client | Connectivity test |
| 11 | FHIR service – NutritionOrder from event | FHIR write |
| 12 | FHIR service – update Timing on optimise | Timing sync |
| 13 | Intake service – skeleton + log | POST /api/intake |
| 14 | Adherence evaluation logic | AdherenceEvaluated event |
| 15 | React – Regimen Builder UI | Form + list |
| 16 | React – Schedule View | Timeline view |
| 17 | React – Intake Logging View | Checklist |
| 18 | Docker Compose – minimal stack | Core infra up |
| 19 | Docker Compose – full topology | All services up |
| 20 | Security – OAuth2 / OIDC | Auth flow |
| 21 | Observability – metrics, logging, tracing | Correlation IDs |
| 22 | Testing – unit + integration harness | Test patterns |
