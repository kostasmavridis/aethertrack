ALTER TABLE intake.outbox_event
    ADD COLUMN IF NOT EXISTS topic VARCHAR(64);

CREATE TABLE IF NOT EXISTS intake.scheduled_dose_ref (
    id                  BIGSERIAL    PRIMARY KEY,
    regimen_item_id     BIGINT       NOT NULL,
    patient_id          VARCHAR(128) NOT NULL,
    timeslot_code       VARCHAR(32)  NOT NULL,
    window_start_time   TIME         NOT NULL,
    window_end_time     TIME         NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sdr_regimen_item
    ON intake.scheduled_dose_ref (regimen_item_id);

CREATE TABLE IF NOT EXISTS intake.adherence_summary (
    id              BIGSERIAL    PRIMARY KEY,
    patient_id      VARCHAR(128) NOT NULL,
    regimen_item_id BIGINT       NOT NULL,
    intake_log_id   BIGINT       NOT NULL,
    outcome         VARCHAR(16)  NOT NULL,   -- ON_TIME | LATE | EARLY | MISSED
    deviation_mins  INTEGER,                 -- signed minutes from window midpoint; NULL if MISSED
    evaluated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_adherence_patient
    ON intake.adherence_summary (patient_id, evaluated_at DESC);

CREATE INDEX IF NOT EXISTS idx_adherence_item
    ON intake.adherence_summary (regimen_item_id);
