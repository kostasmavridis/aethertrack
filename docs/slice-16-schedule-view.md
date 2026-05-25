# Slice 16 – Schedule View

## Goal

Expose a backend read-model endpoint at `GET /api/regimens/{id}/schedule` and add
a React schedule timeline for one day, with time-window columns and supplement rows.

## Backend

### New files

| File | Purpose |
|------|---------|
| `api/schedule/RegimenScheduleResponse` | Top-level DTO |
| `api/schedule/ScheduleRowResponse` | One supplement row |
| `api/schedule/ScheduleAssignmentResponse` | One cell in the time-grid |
| `domain/ScheduledDose` | JPA entity mapped to `supplement.scheduled_dose` |
| `repository/ScheduledDoseRepository` | Reads one-day assignments |
| `service/RegimenScheduleQueryService` | Builds timeline DTO + optimization notes |
| `api/RegimenScheduleController` | `GET /api/regimens/{regimenId}/schedule` |
| `db/migration/V3__create_scheduled_dose.sql` | Adds `scheduled_dose` table |
| `RegimenScheduleQueryServiceTest` | Verifies DTO assembly |

### Response shape

```json
{
  "regimenId": 1,
  "patientId": "patient-1",
  "regimenName": "Morning Stack",
  "windows": ["MORNING", "MIDDAY", "EVENING", "NIGHT"],
  "rows": [
    {
      "regimenItemId": 10,
      "supplementCode": "VIT-D3",
      "supplementName": "VIT-D3",
      "assignments": [
        { "window": "MORNING", "label": "2000 IU · soft score: grouped with breakfast", "assigned": true },
        { "window": "MIDDAY",  "label": "—", "assigned": false }
      ]
    }
  ],
  "optimizationNotes": ["soft score: grouped with breakfast"]
}
```

## Frontend

### New pieces

| File | Purpose |
|------|---------|
| `hooks/useRegimenSchedule.ts` | Fetches a selected regimen's schedule |
| `components/ScheduleTimeline.tsx` | Time-grid table + optimization notes panel |
| `components/ScheduleTimeline.css` | Timeline styles |
| `pages/RegimensPage.tsx` | Adds per-regimen “View Schedule” toggle |
| `pages/RegimensPage.css` | Adds schedule panel layout |
| `api/regimens.ts` | Adds `getRegimenSchedule()` |
| `types/api.ts` | Adds schedule DTO types |

### UX

- Each regimen card gets a **View Schedule** button.
- Clicking it fetches `GET /api/regimens/{id}/schedule` and expands a one-day matrix.
- Assigned cells are blue, unassigned cells are grey.
- `optimizationNotes` render below as plain-text bullets.

## Run tests

```bash
cd backend
./mvnw -pl supplement-domain-service test -Dtest=RegimenScheduleQueryServiceTest
```
