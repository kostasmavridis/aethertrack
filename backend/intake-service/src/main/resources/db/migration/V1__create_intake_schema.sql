CREATE SCHEMA IF NOT EXISTS intake;

CREATE TABLE IF NOT EXISTS intake.intake_log (
    id BIGSERIAL PRIMARY KEY,
    patient_id VARCHAR(128) NOT NULL,
    regimen_item_id BIGINT NOT NULL,
    taken_date_time TIMESTAMPTZ NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_intake_log_patient_time
    ON intake.intake_log (patient_id, taken_date_time DESC);

CREATE INDEX IF NOT EXISTS idx_intake_log_regimen_item
    ON intake.intake_log (regimen_item_id);

CREATE TABLE IF NOT EXISTS intake.outbox_event (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    correlation_id VARCHAR(128),
    payload TEXT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_intake_outbox_status_created
    ON intake.outbox_event (status, created_at);
