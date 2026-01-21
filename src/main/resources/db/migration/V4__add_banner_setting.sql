-- Banner Setting table (singleton)
CREATE TABLE IF NOT EXISTS banner_setting (
    id BIGINT PRIMARY KEY DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT banner_setting_singleton CHECK (id = 1)
);

-- Insert default value
INSERT INTO banner_setting (id, is_active) VALUES (1, FALSE)
ON DUPLICATE KEY UPDATE id = id;
