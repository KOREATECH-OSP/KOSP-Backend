CREATE TABLE organization_bookmarks (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    org_id      BIGINT       NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_org_bookmark (user_id, org_id),
    CONSTRAINT fk_org_bookmark_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_org_bookmark_org  FOREIGN KEY (org_id)  REFERENCES organizations (id) ON DELETE CASCADE
);
