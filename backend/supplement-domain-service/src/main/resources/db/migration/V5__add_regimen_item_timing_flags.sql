-- V5 – Add timing flag columns referenced by scheduling-service views
-- night_time_required: item should be taken at night / before sleep
-- meal_required: item must be taken with food

ALTER TABLE regimen_item
    ADD COLUMN IF NOT EXISTS night_time_required BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS meal_required        BOOLEAN NOT NULL DEFAULT FALSE;
