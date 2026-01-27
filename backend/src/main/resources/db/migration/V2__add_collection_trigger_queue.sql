-- 테이블 기반 트리거 큐 (Backend → Harvester 통신)
CREATE TABLE collection_trigger_queue (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_trigger_queue_status ON collection_trigger_queue (status) WHERE status = 'PENDING';
