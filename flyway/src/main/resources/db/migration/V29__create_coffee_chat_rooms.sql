CREATE TABLE coffee_chat_rooms (
    id          BIGSERIAL PRIMARY KEY,
    user1_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user2_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    last_message      TEXT,
    last_message_at   TIMESTAMP,
    last_sender_id    BIGINT,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    CONSTRAINT uq_coffee_chat_users UNIQUE (user1_id, user2_id),
    CONSTRAINT chk_user_order CHECK (user1_id < user2_id)
);

CREATE INDEX idx_coffee_chat_rooms_user1 ON coffee_chat_rooms(user1_id);
CREATE INDEX idx_coffee_chat_rooms_user2 ON coffee_chat_rooms(user2_id);
