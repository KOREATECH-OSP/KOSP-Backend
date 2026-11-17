CREATE TABLE organization_bookmarks (
    id          BIGSERIAL    NOT NULL,
    user_id     BIGINT       NOT NULL,
    org_id      BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (user_id, org_id),
    CONSTRAINT fk_org_bookmark_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_org_bookmark_org  FOREIGN KEY (org_id)  REFERENCES organizations (id) ON DELETE CASCADE
);
