-- ============================================================
-- V3 – Transactional Outbox: outbox_event table
-- ============================================================
CREATE TABLE outbox_event (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(100)    NOT NULL,
    aggregate_id    VARCHAR(100)    NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    version         VARCHAR(20)     NOT NULL DEFAULT 'v1',
    correlation_id  VARCHAR(100),
    payload         JSONB           NOT NULL,
    topic           VARCHAR(255)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMPTZ,
    retry_count     INT             NOT NULL DEFAULT 0,
    last_error      TEXT
);

CREATE INDEX idx_outbox_status_created ON outbox_event(status, created_at)
    WHERE status = 'PENDING';
CREATE INDEX idx_outbox_aggregate ON outbox_event(aggregate_type, aggregate_id);
