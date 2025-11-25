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
    CONSTRAINT pk_user PRIMARY KEY (id)
);

ALTER TABLE user
    ADD CONSTRAINT uc_user_kut UNIQUE (kut_id);

ALTER TABLE user
    ADD CONSTRAINT uc_user_kut_email UNIQUE (kut_email);
