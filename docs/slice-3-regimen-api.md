# Slice 3 – Regimen Creation API (sync, no events)

## Endpoint

`POST /api/regimens`

### Request body

```json
{
  "patientId": "patient-123",
  "name": "Morning stack",
  "items": [
    {
      "supplementId": 1,
      "doseQty": 1.0,
      "doseUnit": "tablet",
      "frequencyPerDay": 1,
      "scheduleWindow": "MORNING"
    }
  ]
}
```

### Response (201 Created)

```json
{
  "id": 42,
  "patientId": "patient-123",
  "name": "Morning stack",
  "status": "DRAFT",
  "items": [
    {
      "id": 100,
      "supplementId": 1,
      "doseQty": 1.0,
      "doseUnit": "tablet",
      "frequencyPerDay": 1,
      "scheduleWindow": "MORNING"
    }
  ]
}
```

## Validation rules

| Field | Rule |
|-------|------|
| `patientId` | Required, non-blank |
| `name` | Required, non-blank |
| `items` | Required, non-empty list |
| `items[*].supplementId` | Required, must match an existing supplement |
| `items[*].doseQty` | Required, > 0 |
| `items[*].doseUnit` | Required |
| `items[*].frequencyPerDay` | Required, >= 1 |
| `items[*].scheduleWindow` | Optional (e.g. MORNING, WITH_MEAL, BEFORE_SLEEP) |

## Error responses

- `400 Bad Request` — Bean Validation failure (missing/invalid fields)
- `400 Bad Request` — Unknown supplement IDs: `{"message": "Unknown supplement IDs: [99999]"}`

## Local verification

```bash
cd infra && docker compose up -d postgres
cd backend && ./mvnw -pl supplement-domain-service spring-boot:run

# Get a valid supplement ID
SUPP_ID=$(curl -s http://localhost:8081/api/supplements | jq '.[0].id')

# Create a regimen
curl -s -X POST http://localhost:8081/api/regimens \
  -H 'Content-Type: application/json' \
  -d "{\"patientId\":\"p1\",\"name\":\"My stack\",\"items\":[{\"supplementId\":$SUPP_ID,\"doseQty\":1,\"doseUnit\":\"tablet\",\"frequencyPerDay\":1}]}" \
  | jq .
```

## Run integration tests

```bash
cd backend
./mvnw -pl supplement-domain-service test -Dtest=RegimenControllerIT
```

## Next slice

Slice 4 – Kafka Event Contracts & Config (publish RegimenCreated after creation)
