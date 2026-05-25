CREATE SCHEMA IF NOT EXISTS fhir;

CREATE TABLE IF NOT EXISTS fhir.regimen_fhir_mapping (
    id BIGSERIAL PRIMARY KEY,
    regimen_id BIGINT NOT NULL UNIQUE,
    nutrition_order_id VARCHAR(64) NOT NULL,
    nutrition_order_url VARCHAR(256),
    fhir_version VARCHAR(16) NOT NULL DEFAULT '5.0.0',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_rfm_regimen_id
    ON fhir.regimen_fhir_mapping (regimen_id);

CREATE TABLE IF NOT EXISTS fhir.fhir_sync_outbox (
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

CREATE INDEX IF NOT EXISTS idx_fhir_sync_outbox_status_created
    ON fhir.fhir_sync_outbox (status, created_at);
