-- Slice 11: fhir-service owns a "fhir" schema.
-- regimen_fhir_mapping tracks the regimen_id <-> FHIR NutritionOrder.id relationship.

CREATE SCHEMA IF NOT EXISTS fhir;

CREATE TABLE IF NOT EXISTS fhir.regimen_fhir_mapping (
    id                   BIGSERIAL    PRIMARY KEY,
    regimen_id           BIGINT       NOT NULL UNIQUE,
    nutrition_order_id   VARCHAR(64)  NOT NULL,   -- FHIR logical ID on HAPI server
    nutrition_order_url  VARCHAR(256),            -- full HAPI resource URL
    fhir_version         VARCHAR(16)  NOT NULL DEFAULT '5.0.0',
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_rfm_regimen_id
    ON fhir.regimen_fhir_mapping (regimen_id);
