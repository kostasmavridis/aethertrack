-- Slice 8: scheduling-service read schema
-- The scheduling-service shares the same Postgres instance as
-- supplement-domain-service but owns a separate schema "scheduling".
-- It reads regimen and regimen_item via views (read-only access).
-- Full table ownership belongs to supplement-domain-service (schema "public").

CREATE SCHEMA IF NOT EXISTS scheduling;

-- Read-only views over supplement-domain-service tables
CREATE OR REPLACE VIEW scheduling.v_regimen AS
SELECT id, patient_id, name, created_at
FROM   public.regimen;

CREATE OR REPLACE VIEW scheduling.v_regimen_item AS
SELECT ri.id            AS item_id,
       ri.regimen_id,
       ri.supplement_id,
       s.code           AS supplement_code,
       s.category       AS supplement_category,
       ri.dose_qty,
       ri.dose_unit,
       ri.frequency_per_day,
       ri.schedule_window,
       ri.night_time_required,
       ri.meal_required
FROM   public.regimen_item ri
JOIN   public.supplement s ON s.id = ri.supplement_id;
