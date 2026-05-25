# Slice 10 – FHIR Service Skeleton + HAPI Client

## Goal

Bootable `fhir-service` with a configured HAPI FHIR R5 `IGenericClient`,
a custom health indicator that checks server connectivity,
and two REST endpoints for manual verification.

## New files

| File | Purpose |
|------|---------|
| `FhirServiceApplication` | Spring Boot entry point; `@ConfigurationPropertiesScan` |
| `config/FhirProperties` | `@ConfigurationProperties` record: `base-url`, `socket-timeout-ms`, `connect-timeout-ms` |
| `config/HapiFhirClientConfig` | `@Bean FhirContext` (R5) + `@Bean IGenericClient`; `ServerValidationModeEnum.NEVER` |
| `health/FhirServerHealthIndicator` | Fetches `CapabilityStatement`; UP with details, DOWN on error |
| `api/FhirConnectivityController` | `GET /api/fhir/connectivity`, `GET /api/fhir/patient/{id}` |
| `application.yml` | Port 8083, `aethertrack.fhir.*` externalized, management probes |
| `HapiFhirClientConfigTest` | Context load test; no real HAPI needed |
| `FhirServerHealthIndicatorTest` | Mockito: UP path, DOWN on exception |
| `FhirConnectivityControllerTest` | MockMvc: 200 OK + fields, 503 when HAPI down |

## How connectivity checking works

```
GET /api/fhir/connectivity
    │
    ► IGenericClient.capabilities().ofType(CapabilityStatement).execute()
    ├─ OK  → 200 { status:UP, fhirVersion, publisher, baseUrl, checkedAt }
    └─ ERR → 503 { status:DOWN, baseUrl, error }

GET /actuator/health  (includes fhirServer component)
    ├─ UP   { fhirVersion:"5.0.0", publisher:"HAPI FHIR", baseUrl }
    └─ DOWN { baseUrl, error }  ← readiness probe fails → k8s holds traffic
```

## Health probe table

| Probe | Path | Behaviour when HAPI DOWN |
|-------|------|---------------------------|
| Liveness  | `/actuator/health/liveness`  | Stays UP (app is alive) |
| Readiness | `/actuator/health/readiness` | Flips DOWN via `fhirServer` component |
| Custom    | `/api/fhir/connectivity`     | Returns 503 JSON with error detail |

## Configuration

```yaml
aethertrack:
  fhir:
    base-url:           ${FHIR_BASE_URL:http://localhost:8080/fhir}
    socket-timeout-ms:  10000
    connect-timeout-ms: 5000
```

Override at runtime:
```bash
FHIR_BASE_URL=http://hapi-fhir:8080/fhir java -jar fhir-service.jar
```

## Running tests

```bash
cd backend
./mvnw -pl fhir-service test \
  -Dtest=HapiFhirClientConfigTest,FhirServerHealthIndicatorTest,FhirConnectivityControllerTest
```

## Smoke test (infra running)

```bash
# Start infra (hapi-fhir already included from Slice 0)
cd infra && docker compose up -d hapi-fhir

# Start service
cd backend && ./mvnw -pl fhir-service spring-boot:run &

# Check connectivity endpoint
curl -s http://localhost:8083/api/fhir/connectivity | jq .
# Expected: { "status": "UP", "fhirVersion": "5.0.0", ... }

# Check Actuator health (includes fhirServer component)
curl -s http://localhost:8083/actuator/health | jq .components.fhirServer
```

## Next slice

Slice 11 – consume `RegimenCreated` events and write `NutritionOrder` FHIR resources
to the HAPI server via this client.
