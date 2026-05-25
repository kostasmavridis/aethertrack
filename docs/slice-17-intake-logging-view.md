# Slice 17 – Intake Logging View

## Goal

Add a “Today’s supplements” checklist with optimistic intake logging and adherence color hints.

## Backend

### New endpoint

`GET /api/regimens/{regimenId}/today`

### Response shape

```json
{
  "regimenId": 1,
  "patientId": "patient-1",
  "regimenName": "Morning Stack",
  "doses": [
    {
      "regimenItemId": 10,
      "supplementCode": "VIT-D3",
      "supplementName": "VIT-D3",
      "window": "MORNING",
      "doseLabel": "2000 IU",
      "taken": true,
      "adherenceStatus": "ON_TIME"
    }
  ]
}
```

### Query behavior

- Reads `supplement.scheduled_dose` for `day_offset = 0`.
- Checks `intake.intake_log` for any rows today (UTC) for the same patient and regimen items.
- Reads latest `intake.adherence_summary` outcomes, if any, and maps them to `adherenceStatus`.

## Frontend

### New pieces

| File | Purpose |
|------|---------|
| `api/intake.ts` | `POST /api/intake` client |
| `hooks/useRegimenToday.ts` | Fetches `/today` payload for selected regimen |
| `components/TodayChecklist.tsx` | Checklist UI + optimistic toggle |
| `components/TodayChecklist.css` | Checklist + adherence pill styling |
| `pages/RegimensPage.tsx` | Expands details panel to show checklist + schedule |

### UX

- Clicking **View Details** opens both the Today checklist and Schedule timeline.
- Clicking a checkbox immediately marks the item as taken in the UI, then posts to `/api/intake`.
- If the POST fails, the optimistic change is rolled back.
- Adherence pill colors: green = `ON_TIME`, amber = `LATE` / `EARLY`, grey = unknown / pending.

## Backend test

```bash
cd backend
./mvnw -pl supplement-domain-service test -Dtest=RegimenTodayQueryServiceTest
```
