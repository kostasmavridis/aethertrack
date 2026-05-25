CREATE TABLE IF NOT EXISTS supplement.scheduled_dose (
    id               BIGSERIAL PRIMARY KEY,
    regimen_id       BIGINT       NOT NULL,
    regimen_item_id  BIGINT       NOT NULL,
    timeslot         VARCHAR(32)  NOT NULL,
    day_offset       INTEGER      NOT NULL DEFAULT 0,
    explanation      VARCHAR(255),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_scheduled_dose_regimen_day
    ON supplement.scheduled_dose (regimen_id, day_offset);
