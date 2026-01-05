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

-- V3: Attachment table (consolidated from V3__add_attachment_table.sql)
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

-- GitHub Statistics Tables
CREATE TABLE github_user_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    github_id VARCHAR(100) NOT NULL UNIQUE,
    
    -- 기본 통계
    total_commits INT NOT NULL DEFAULT 0,
    total_lines INT NOT NULL DEFAULT 0,
    total_additions INT NOT NULL DEFAULT 0,
    total_deletions INT NOT NULL DEFAULT 0,
    total_prs INT NOT NULL DEFAULT 0,
    total_issues INT NOT NULL DEFAULT 0,
    
    -- 레포지토리 통계
    owned_repos_count INT NOT NULL DEFAULT 0,
    contributed_repos_count INT NOT NULL DEFAULT 0,
    total_stars_received INT NOT NULL DEFAULT 0,
    
    -- 시간대 분석
    night_commits INT NOT NULL DEFAULT 0,
    day_commits INT NOT NULL DEFAULT 0,
    
    -- 점수
    total_score DECIMAL(10, 2) NOT NULL DEFAULT 0,
    
    -- 메타
    calculated_at DATETIME NOT NULL,
    data_period_start DATE,
    data_period_end DATE,
    
    INDEX idx_github_id (github_id),
    INDEX idx_total_score (total_score DESC),
    INDEX idx_calculated_at (calculated_at)
);

CREATE TABLE github_monthly_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    github_id VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    
    commits_count INT NOT NULL DEFAULT 0,
    lines_count INT NOT NULL DEFAULT 0,
    additions_count INT NOT NULL DEFAULT 0,
    deletions_count INT NOT NULL DEFAULT 0,
    prs_count INT NOT NULL DEFAULT 0,
    issues_count INT NOT NULL DEFAULT 0,
    created_repos_count INT NOT NULL DEFAULT 0,
    contributed_repos_count INT NOT NULL DEFAULT 0,
    
    calculated_at DATETIME NOT NULL,
    
    UNIQUE KEY uk_user_month (github_id, year, month),
    INDEX idx_github_id (github_id),
    INDEX idx_year_month (year, month)
);

CREATE TABLE github_repository_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    repo_owner VARCHAR(100) NOT NULL,
    repo_name VARCHAR(200) NOT NULL,
    
    stargazers_count INT NOT NULL DEFAULT 0,
    forks_count INT NOT NULL DEFAULT 0,
    watchers_count INT NOT NULL DEFAULT 0,
    commits_count INT NOT NULL DEFAULT 0,
    contributors_count INT NOT NULL DEFAULT 0,
    
    open_issues_count INT NOT NULL DEFAULT 0,
    closed_issues_count INT NOT NULL DEFAULT 0,
    open_prs_count INT NOT NULL DEFAULT 0,
    closed_prs_count INT NOT NULL DEFAULT 0,
    
    primary_language VARCHAR(50),
    license VARCHAR(100),
    created_at DATETIME,
    updated_at DATETIME,
    
    calculated_at DATETIME NOT NULL,
    
    UNIQUE KEY uk_repo (repo_owner, repo_name),
    INDEX idx_owner (repo_owner),
    INDEX idx_stars (stargazers_count DESC)
);

CREATE TABLE github_collection_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    github_id VARCHAR(100) NOT NULL UNIQUE,
    
    initial_collected BOOLEAN NOT NULL DEFAULT FALSE,
    last_collected_at DATETIME,
    last_commit_sha VARCHAR(40),
    
    total_api_calls INT NOT NULL DEFAULT 0,
    last_error TEXT,
    last_error_at DATETIME,
    
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_github_id (github_id),
    INDEX idx_initial_collected (initial_collected)
);

-- 저장소별 통계 테이블
CREATE TABLE github_repository_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    repo_owner VARCHAR(100) NOT NULL,
    repo_name VARCHAR(200) NOT NULL,
    contributor_github_id VARCHAR(100) NOT NULL,

    stargazers_count INT NOT NULL DEFAULT 0,
    forks_count INT NOT NULL DEFAULT 0,
    watchers_count INT NOT NULL DEFAULT 0,

    total_commits_count INT NOT NULL DEFAULT 0,
    total_prs_count INT NOT NULL DEFAULT 0,
    total_issues_count INT NOT NULL DEFAULT 0,

    user_commits_count INT NOT NULL DEFAULT 0,
    user_prs_count INT NOT NULL DEFAULT 0,
    user_issues_count INT NOT NULL DEFAULT 0,
    last_commit_date DATETIME,

    description VARCHAR(500),
    primary_language VARCHAR(50),

    calculated_at DATETIME NOT NULL,

    UNIQUE KEY uk_repo_contributor (repo_owner, repo_name, contributor_github_id),
    INDEX idx_contributor (contributor_github_id),
    INDEX idx_last_commit (last_commit_date DESC),
    INDEX idx_stars (stargazers_count DESC)
);

-- 점수 설정 테이블
CREATE TABLE github_score_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_name VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT FALSE,

    activity_level_max_score DOUBLE NOT NULL DEFAULT 3.0,
    commits_weight DOUBLE NOT NULL DEFAULT 0.01,
    lines_weight DOUBLE NOT NULL DEFAULT 0.0001,

    diversity_max_score DOUBLE NOT NULL DEFAULT 1.0,
    diversity_repo_threshold INT NOT NULL DEFAULT 10,

    impact_max_score DOUBLE NOT NULL DEFAULT 5.0,
    stars_weight DOUBLE NOT NULL DEFAULT 0.01,
    forks_weight DOUBLE NOT NULL DEFAULT 0.05,
    contributors_weight DOUBLE NOT NULL DEFAULT 0.02,

    night_owl_bonus DOUBLE NOT NULL DEFAULT 0.5,
    early_adopter_bonus DOUBLE NOT NULL DEFAULT 0.3,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    created_by VARCHAR(100),

    INDEX idx_active (active)
);

-- 기본 설정 삽입
INSERT INTO github_score_config (
    config_name, active,
    activity_level_max_score, commits_weight, lines_weight,
    diversity_max_score, diversity_repo_threshold,
    impact_max_score, stars_weight, forks_weight,
    night_owl_bonus, created_by
) VALUES (
    'default', TRUE,
    3.0, 0.01, 0.0001,
    1.0, 10,
    5.0, 0.01, 0.05,
    0.5, 'system'
);
