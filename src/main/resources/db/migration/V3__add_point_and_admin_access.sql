-- V3: Add point system and admin access control

-- 1. Add point column to users table
ALTER TABLE users ADD COLUMN point INT NOT NULL DEFAULT 0;

-- 2. Add canAccessAdmin column to role table
ALTER TABLE role ADD COLUMN can_access_admin BIT(1) NOT NULL DEFAULT 0;

-- 3. Create point_transactions table
CREATE TABLE point_transactions (
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    TIMESTAMP             NOT NULL,
    updated_at    TIMESTAMP             NOT NULL,
    user_id       BIGINT                NOT NULL,
    amount        INT                   NOT NULL,
    type          VARCHAR(20)           NOT NULL,
    source        VARCHAR(20)           NOT NULL,
    reason        VARCHAR(255)          NOT NULL,
    balance_after INT                   NOT NULL,
    CONSTRAINT pk_point_transactions PRIMARY KEY (id)
);

ALTER TABLE point_transactions
    ADD CONSTRAINT fk_point_transactions_user FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_point_transactions_user ON point_transactions (user_id);
CREATE INDEX idx_point_transactions_created_at ON point_transactions (created_at DESC);
