CREATE TABLE code_reviews (
    id              BIGSERIAL PRIMARY KEY,
    repo_owner      VARCHAR(255) NOT NULL,
    repository_name VARCHAR(255) NOT NULL,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content         TEXT NOT NULL,
    likes_count     INTEGER NOT NULL DEFAULT 0,
    parent_id       BIGINT REFERENCES code_reviews(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_code_review_repo ON code_reviews(repo_owner, repository_name);
CREATE INDEX idx_code_review_parent ON code_reviews(parent_id);

CREATE TABLE code_review_likes (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code_review_id  BIGINT NOT NULL REFERENCES code_reviews(id) ON DELETE CASCADE,
    CONSTRAINT uq_code_review_like UNIQUE (user_id, code_review_id)
);
