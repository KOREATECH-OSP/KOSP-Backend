-- V3: Add attachment table for S3 file upload
CREATE TABLE attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL UNIQUE,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    url VARCHAR(500) NOT NULL,
    article_id BIGINT,
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

CREATE INDEX idx_attachment_article ON attachment(article_id);
CREATE INDEX idx_attachment_uploader ON attachment(uploaded_by);
