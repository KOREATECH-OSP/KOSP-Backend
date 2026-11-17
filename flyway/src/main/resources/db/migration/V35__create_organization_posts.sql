CREATE TABLE organization_posts
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT       NOT NULL,
    author_id       BIGINT       NOT NULL,
    category        VARCHAR(20)  NOT NULL,
    visibility      VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
    title           VARCHAR(200) NOT NULL,
    description     VARCHAR(500),
    content         TEXT         NOT NULL,
    comments_count  INT          NOT NULL DEFAULT 0,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    CONSTRAINT fk_org_post_org FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_org_post_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE TABLE organization_post_files
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id   BIGINT       NOT NULL,
    file_name VARCHAR(300) NOT NULL,
    file_url  VARCHAR(500) NOT NULL,
    CONSTRAINT fk_org_post_file_post FOREIGN KEY (post_id) REFERENCES organization_posts (id) ON DELETE CASCADE
);
