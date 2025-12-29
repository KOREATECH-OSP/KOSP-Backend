CREATE TABLE users
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   TIMESTAMP             NOT NULL,
    updated_at   TIMESTAMP             NOT NULL,
    name         VARCHAR(50)           NOT NULL,
    kut_id       VARCHAR(255)          NOT NULL,
    kut_email    VARCHAR(255)          NOT NULL,
    password     VARCHAR(255)          NOT NULL,
    introduction VARCHAR(255)          NULL,
    github_id    BIGINT                NOT NULL,
    is_deleted   BIT(1)                NOT NULL,
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
    is_deleted     BIT(1)                NOT NULL,
    is_pinned      BIT(1)                NOT NULL,
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

CREATE TABLE permission
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  TIMESTAMP             NOT NULL,
    updated_at  TIMESTAMP             NOT NULL,
    name        VARCHAR(255)          NOT NULL,
    description VARCHAR(255)          NULL,
    CONSTRAINT pk_permission PRIMARY KEY (id)
);

ALTER TABLE permission
    ADD CONSTRAINT uc_permission_name UNIQUE (name);

CREATE TABLE policy
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  TIMESTAMP             NOT NULL,
    updated_at  TIMESTAMP             NOT NULL,
    name        VARCHAR(255)          NOT NULL,
    description VARCHAR(255)          NULL,
    CONSTRAINT pk_policy PRIMARY KEY (id)
);

ALTER TABLE policy
    ADD CONSTRAINT uc_policy_name UNIQUE (name);

CREATE TABLE role
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  TIMESTAMP             NOT NULL,
    updated_at  TIMESTAMP             NOT NULL,
    name        VARCHAR(255)          NOT NULL,
    description VARCHAR(255)          NULL,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

ALTER TABLE role
    ADD CONSTRAINT uc_role_name UNIQUE (name);

CREATE TABLE policy_permission
(
    policy_id     BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT pk_policy_permission PRIMARY KEY (policy_id, permission_id)
);

ALTER TABLE policy_permission
    ADD CONSTRAINT fk_policy_permission_on_policy FOREIGN KEY (policy_id) REFERENCES policy (id);

ALTER TABLE policy_permission
    ADD CONSTRAINT fk_policy_permission_on_permission FOREIGN KEY (permission_id) REFERENCES permission (id);

CREATE TABLE role_policy
(
    role_id   BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    CONSTRAINT pk_role_policy PRIMARY KEY (role_id, policy_id)
);

ALTER TABLE role_policy
    ADD CONSTRAINT fk_role_policy_on_role FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE role_policy
    ADD CONSTRAINT fk_role_policy_on_policy FOREIGN KEY (policy_id) REFERENCES policy (id);

CREATE TABLE user_role
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (user_id, role_id)
);

ALTER TABLE user_role
    ADD CONSTRAINT fk_user_role_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_role
    ADD CONSTRAINT fk_user_role_on_role FOREIGN KEY (role_id) REFERENCES role (id);

CREATE TABLE article_like
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at TIMESTAMP             NOT NULL,
    updated_at TIMESTAMP             NOT NULL,
    user_id    BIGINT                NOT NULL,
    article_id BIGINT                NOT NULL,
    CONSTRAINT pk_article_like PRIMARY KEY (id)
);

ALTER TABLE article_like
    ADD CONSTRAINT fk_article_like_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE article_like
    ADD CONSTRAINT fk_article_like_on_article FOREIGN KEY (article_id) REFERENCES article (id);

CREATE TABLE article_bookmark
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at TIMESTAMP             NOT NULL,
    updated_at TIMESTAMP             NOT NULL,
    user_id    BIGINT                NOT NULL,
    article_id BIGINT                NOT NULL,
    CONSTRAINT pk_article_bookmark PRIMARY KEY (id)
);

ALTER TABLE article_bookmark
    ADD CONSTRAINT fk_article_bookmark_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE article_bookmark
    ADD CONSTRAINT fk_article_bookmark_on_article FOREIGN KEY (article_id) REFERENCES article (id);

CREATE TABLE comment
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at TIMESTAMP             NOT NULL,
    updated_at TIMESTAMP             NOT NULL,
    article_id BIGINT                NOT NULL,
    author_id  BIGINT                NOT NULL,
    content    TEXT                  NOT NULL,
    likes      INT                   NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id)
);

ALTER TABLE comment
    ADD CONSTRAINT fk_comment_on_article FOREIGN KEY (article_id) REFERENCES article (id);

ALTER TABLE comment
    ADD CONSTRAINT fk_comment_on_author FOREIGN KEY (author_id) REFERENCES users (id);

CREATE TABLE comment_like
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at TIMESTAMP             NOT NULL,
    updated_at TIMESTAMP             NOT NULL,
    user_id    BIGINT                NOT NULL,
    comment_id BIGINT                NOT NULL,
    CONSTRAINT pk_comment_like PRIMARY KEY (id)
);

ALTER TABLE comment_like
    ADD CONSTRAINT fk_comment_like_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE comment_like
    ADD CONSTRAINT fk_comment_like_on_comment FOREIGN KEY (comment_id) REFERENCES comment (id);

CREATE TABLE team
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  TIMESTAMP             NOT NULL,
    updated_at  TIMESTAMP             NOT NULL,
    name        VARCHAR(50)           NOT NULL,
    description VARCHAR(255)          NOT NULL,
    image_url   VARCHAR(255)          NULL,
    CONSTRAINT pk_team PRIMARY KEY (id)
);

CREATE TABLE team_member
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at TIMESTAMP             NOT NULL,
    updated_at TIMESTAMP             NOT NULL,
    team_id    BIGINT                NOT NULL,
    user_id    BIGINT                NOT NULL,
    role       VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_team_member PRIMARY KEY (id)
);

ALTER TABLE team_member
    ADD CONSTRAINT fk_team_member_on_team FOREIGN KEY (team_id) REFERENCES team (id);

ALTER TABLE team_member
    ADD CONSTRAINT fk_team_member_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE recruit
    ADD CONSTRAINT FK_RECRUIT_ON_TEAM FOREIGN KEY (team_id) REFERENCES team (id);

CREATE TABLE challenge
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  TIMESTAMP             NOT NULL,
    updated_at  TIMESTAMP             NOT NULL,
    name        VARCHAR(255)          NOT NULL,
    description VARCHAR(255)          NOT NULL,
    `condition` TEXT                  NOT NULL,
    tier        INT                   NOT NULL,
    image_url   VARCHAR(255)          NULL,
    CONSTRAINT pk_challenge PRIMARY KEY (id)
);

CREATE TABLE challenge_history
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   TIMESTAMP             NOT NULL,
    updated_at   TIMESTAMP             NOT NULL,
    user_id      BIGINT                NOT NULL,
    challenge_id BIGINT                NOT NULL,
    is_achieved  BIT(1)                NOT NULL,
    achieved_at  TIMESTAMP             NULL,
    CONSTRAINT pk_challenge_history PRIMARY KEY (id)
);

ALTER TABLE challenge_history
    ADD CONSTRAINT fk_challenge_history_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE challenge_history
    ADD CONSTRAINT fk_challenge_history_on_challenge FOREIGN KEY (challenge_id) REFERENCES challenge (id);

CREATE TABLE recruit_apply
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    TIMESTAMP             NOT NULL,
    updated_at    TIMESTAMP             NOT NULL,
    recruit_id    BIGINT                NOT NULL,
    user_id       BIGINT                NOT NULL,
    status        VARCHAR(255)          NOT NULL,
    reason        VARCHAR(255)          NOT NULL,
    portfolio_url VARCHAR(255)          NULL,
    CONSTRAINT pk_recruit_apply PRIMARY KEY (id)
);

ALTER TABLE recruit_apply
    ADD CONSTRAINT fk_recruit_apply_on_recruit FOREIGN KEY (recruit_id) REFERENCES recruit (id);

ALTER TABLE recruit_apply
    ADD CONSTRAINT fk_recruit_apply_on_user FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE report
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   TIMESTAMP             NOT NULL,
    updated_at   TIMESTAMP             NOT NULL,
    reporter_id  BIGINT                NOT NULL,
    target_type  VARCHAR(255)          NOT NULL,
    target_id    BIGINT                NOT NULL,
    reason       VARCHAR(255)          NOT NULL,
    description  TEXT                  NULL,
    status       VARCHAR(255)          NOT NULL,
    processed_at TIMESTAMP             NULL,
    CONSTRAINT pk_report PRIMARY KEY (id)
);

ALTER TABLE report
    ADD CONSTRAINT fk_report_on_reporter FOREIGN KEY (reporter_id) REFERENCES users (id);
