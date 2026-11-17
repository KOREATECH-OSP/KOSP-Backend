CREATE TABLE organization_post_comments
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT      NOT NULL,
    author_id  BIGINT      NOT NULL,
    content    TEXT        NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_org_comment_post FOREIGN KEY (post_id) REFERENCES organization_posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_org_comment_author FOREIGN KEY (author_id) REFERENCES users (id)
);
