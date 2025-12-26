CREATE TABLE user
(
    id         INT AUTO_INCREMENT NOT NULL,
    created_at TIMESTAMP          NOT NULL,
    updated_at TIMESTAMP          NOT NULL,
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
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL,
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
CREATE TABLE board
(
    id                     BIGINT AUTO_INCREMENT NOT NULL,
    created_at             TIMESTAMP             NOT NULL,
    updated_at             TIMESTAMP             NOT NULL,
    name                   VARCHAR(30)           NOT NULL,
    description            VARCHAR(255)          NOT NULL,
    is_recruit_allowed BIT(1)                NOT NULL,
    CONSTRAINT pk_board PRIMARY KEY (id)
);

CREATE TABLE article
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    created_at     TIMESTAMP             NOT NULL,
    updated_at     TIMESTAMP             NOT NULL,
    author_id      INT                   NOT NULL,
    category       VARCHAR(255)          NOT NULL,
    board_id       BIGINT                NOT NULL,
    author_id      BIGINT                NOT NULL,
    dtype          VARCHAR(31)           NOT NULL,
    title          VARCHAR(255)          NOT NULL,
    body           TEXT                  NOT NULL,
    views          INT                   NOT NULL,
    likes          INT                   NOT NULL,
    comments_count INT                   NOT NULL,
    CONSTRAINT pk_article PRIMARY KEY (id)
);

ALTER TABLE article
    ADD CONSTRAINT FK_ARTICLE_ON_BOARD FOREIGN KEY (board_id) REFERENCES board (id);

ALTER TABLE article
    ADD CONSTRAINT FK_ARTICLE_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES users (id);

