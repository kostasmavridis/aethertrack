-- ============================================================
-- V1 – Initial schema: supplement, regimen, regimen_item
-- ============================================================

CREATE TABLE supplement (
    id              BIGSERIAL       PRIMARY KEY,
    code            VARCHAR(100)    NOT NULL UNIQUE,
    name            VARCHAR(255)    NOT NULL,
    category        VARCHAR(100)    NOT NULL,          -- e.g. VITAMIN, MINERAL, AMINO_ACID
    description     TEXT,
    nutrients       JSONB,                              -- [{substance, amount, unit}]
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_supplement_category ON supplement(category);
CREATE INDEX idx_supplement_code     ON supplement(code);
CREATE INDEX idx_supplement_nutrients ON supplement USING GIN(nutrients);

-- ─────────────────────────────────────────────────
CREATE TABLE regimen (
    id              BIGSERIAL       PRIMARY KEY,
    patient_id      VARCHAR(100)    NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    status          VARCHAR(50)     NOT NULL DEFAULT 'DRAFT',   -- DRAFT | ACTIVE | ARCHIVED
    care_plan_id    VARCHAR(100),                                -- FHIR CarePlan.id (populated by fhir-service)
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_regimen_patient ON regimen(patient_id);

-- ─────────────────────────────────────────────────
CREATE TABLE regimen_item (
    id                  BIGSERIAL       PRIMARY KEY,
    regimen_id          BIGINT          NOT NULL REFERENCES regimen(id) ON DELETE CASCADE,
    supplement_id       BIGINT          NOT NULL REFERENCES supplement(id),
    dose_qty            NUMERIC(10,3)   NOT NULL,
    dose_unit           VARCHAR(50)     NOT NULL DEFAULT 'mg',
    frequency_per_day   INT             NOT NULL DEFAULT 1,
    schedule_window     VARCHAR(100),                          -- e.g. MORNING, WITH_MEAL, BEFORE_SLEEP
    nutrition_order_id  VARCHAR(100),                          -- FHIR NutritionOrder.id
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_regimen_supplement UNIQUE (regimen_id, supplement_id)
);

CREATE INDEX idx_regimen_item_regimen    ON regimen_item(regimen_id);
CREATE INDEX idx_regimen_item_supplement ON regimen_item(supplement_id);
