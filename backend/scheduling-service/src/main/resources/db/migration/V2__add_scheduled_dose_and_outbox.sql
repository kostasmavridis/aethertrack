-- Slice 9: persistence for solved schedule + transactional outbox
-- Both tables live in the "scheduling" schema owned by scheduling-service.

-- ── scheduled_dose ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS scheduling.scheduled_dose (
    id              BIGSERIAL       PRIMARY KEY,
    regimen_id      BIGINT          NOT NULL,
    regimen_item_id BIGINT          NOT NULL,
    timeslot        VARCHAR(32)     NOT NULL,
    timeslot_start  TIME            NOT NULL,
    timeslot_end    TIME            NOT NULL,
    day_offset      INT             NOT NULL DEFAULT 0,
    hard_score      INT             NOT NULL DEFAULT 0,
    soft_score      INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT fk_sd_regimen_item
        FOREIGN KEY (regimen_item_id)
        REFERENCES public.regimen_item(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_scheduled_dose_regimen_id
    ON scheduling.scheduled_dose (regimen_id);

CREATE INDEX IF NOT EXISTS idx_scheduled_dose_regimen_item
    ON scheduling.scheduled_dose (regimen_item_id);

-- ── outbox_event ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS scheduling.outbox_event (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type      VARCHAR(64)     NOT NULL,
    aggregate_type  VARCHAR(64)     NOT NULL,
    aggregate_id    VARCHAR(64)     NOT NULL,
    correlation_id  VARCHAR(64),
    payload         JSONB           NOT NULL,
    status          VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    sent_at         TIMESTAMPTZ,
    retry_count     INT             NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_outbox_status
    ON scheduling.outbox_event (status, created_at)
    WHERE status = 'PENDING';
