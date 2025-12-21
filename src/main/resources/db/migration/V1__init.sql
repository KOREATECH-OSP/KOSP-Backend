CREATE TABLE user
(
    id         INT AUTO_INCREMENT NOT NULL,
    created_at timestamp          NOT NULL,
    updated_at timestamp          NOT NULL,
    name       VARCHAR(50)        NOT NULL,
    kut_id     VARCHAR(255)       NOT NULL,
    kut_email  VARCHAR(255)       NOT NULL,
    password   VARCHAR(255)       NOT NULL,
    is_deleted BIT(1)             NOT NULL,
    github_id  BIGINT             NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE github_user
(
    github_id          BIGINT       NOT NULL,
    created_at         timestamp    NOT NULL,
    updated_at         timestamp    NOT NULL,
    github_login       VARCHAR(255) NULL,
    github_name        VARCHAR(255) NULL,
    github_avatar_url VARCHAR(255) NULL,
    github_token       TEXT         NULL,
    last_crawling      datetime     NULL,
    CONSTRAINT pk_github_user PRIMARY KEY (github_id)
);

ALTER TABLE user
    ADD CONSTRAINT uc_user_github UNIQUE (github_id);

ALTER TABLE user
    ADD CONSTRAINT uc_user_kut UNIQUE (kut_id);

ALTER TABLE user
    ADD CONSTRAINT uc_user_kut_email UNIQUE (kut_email);

ALTER TABLE user
    ADD CONSTRAINT FK_USER_ON_GITHUB FOREIGN KEY (github_id) REFERENCES github_user (github_id);
