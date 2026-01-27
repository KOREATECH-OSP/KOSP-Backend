-- 테이블 기반 트리거 큐 (Backend → Harvester 통신)
CREATE TABLE collection_trigger (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    priority VARCHAR(10) NOT NULL DEFAULT 'HIGH',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    scheduled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trigger_pending ON collection_trigger (status, scheduled_at) WHERE status = 'PENDING';
