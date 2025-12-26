CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at TIMESTAMP          NOT NULL,
    updated_at TIMESTAMP          NOT NULL,
    name       VARCHAR(50)        NOT NULL,
    kut_id     VARCHAR(255)       NOT NULL,
    kut_email  VARCHAR(255)       NOT NULL,
    password   VARCHAR(255)       NOT NULL,
    is_deleted BIT(1)             NOT NULL,
    github_id  BIGINT             NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
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

ALTER TABLE users
    ADD CONSTRAINT uc_users_github UNIQUE (github_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_kut UNIQUE (kut_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_kut_email UNIQUE (kut_email);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_GITHUB FOREIGN KEY (github_id) REFERENCES github_user (github_id);

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
    board_id       BIGINT                NOT NULL,
    author_id      BIGINT                NOT NULL,
    dtype          VARCHAR(31)           NOT NULL,
    title          VARCHAR(255)          NOT NULL,
    content        MEDIUMTEXT            NOT NULL,
    views          INT                   NOT NULL,
    likes          INT                   NOT NULL,
    comments_count INT                   NOT NULL,
    CONSTRAINT pk_article PRIMARY KEY (id)
);

ALTER TABLE article
    ADD CONSTRAINT FK_ARTICLE_ON_BOARD FOREIGN KEY (board_id) REFERENCES board (id);

ALTER TABLE article
    ADD CONSTRAINT FK_ARTICLE_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES users (id);

CREATE TABLE recruit
(
    id         BIGINT       NOT NULL,
    team_id    BIGINT       NOT NULL,
    status     VARCHAR(255) NOT NULL,
    start_date TIMESTAMP    NOT NULL,
    end_date   TIMESTAMP    NULL,
    CONSTRAINT pk_recruit PRIMARY KEY (id)
);

ALTER TABLE recruit
    ADD CONSTRAINT FK_RECRUIT_ON_ARTICLE FOREIGN KEY (id) REFERENCES article (id);

CREATE TABLE article_tags
(
    article_id BIGINT       NOT NULL,
    tag        VARCHAR(255) NULL
);

ALTER TABLE article_tags
    ADD CONSTRAINT fk_article_tags_on_article FOREIGN KEY (article_id) REFERENCES article (id);
