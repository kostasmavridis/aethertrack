# Slice 15 – React Regimen Builder UI

## Goal

Frontend form to create a regimen and view the list of existing regimens.

## Routes

| Path | Page |
|------|------|
| `/` | `RegimensPage` – lists all regimens via `GET /api/regimens` |
| `/regimens/new` | `NewRegimenPage` – form to create a regimen via `POST /api/regimens` |

## New files

| File | Purpose |
|------|---------|
| `src/main.tsx` | App entry point wrapped in `BrowserRouter` |
| `src/App.tsx` + `App.css` | Shell layout with sticky top-nav and route declarations |
| `src/index.css` | Root height reset |
| `src/types/api.ts` | Shared TypeScript types aligned with backend DTOs |
| `src/api/client.ts` | Base `fetch` wrapper with `ApiError` |
| `src/api/supplements.ts` | `GET /api/supplements` + 6-item static fallback |
| `src/api/regimens.ts` | `POST /api/regimens` + `GET /api/regimens` |
| `src/hooks/useSupplements.ts` | Fetches supplements; falls back to static list on error |
| `src/hooks/useRegimens.ts` | Fetches regimen list with `refresh()` callback |
| `src/components/SupplementItemRow.tsx` | Per-row form: supplement select, dose, unit, frequency, window |
| `src/components/SupplementItemRow.css` | Row styles |
| `src/pages/NewRegimenPage.tsx` | Regimen creation form with success banner |
| `src/pages/NewRegimenPage.css` | Form page styles |
| `src/pages/RegimensPage.tsx` | Regimen list with per-regimen item table |
| `src/pages/RegimensPage.css` | List styles including status badge colours |

## Key design decisions

- **Static supplement fallback** – if `GET /api/supplements` errors (backend not running), `useSupplements` silently uses a hard-coded list of 6 common supplements.
- **Schedule window hint** – each supplement row offers a dropdown: MORNING / MIDDAY / EVENING / NIGHT / WITH_MEAL, mapping directly to `scheduleWindow` used by the scheduling-service.
- **Success banner** – after a successful `POST /api/regimens` the form is replaced with a green confirmation showing `regimenId`.
- **Status badges** – regimen list uses colour-coded pills: green = ACTIVE, yellow = PENDING, grey = INACTIVE.

## API proxy

All `/api/*` calls are proxied to `http://localhost:8081` by the Vite config from Slice 0. No CORS headers needed during local development.

## Run locally

```bash
cd frontend
pnpm install
pnpm dev
# → http://localhost:5173
```
