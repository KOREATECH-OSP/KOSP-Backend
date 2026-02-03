ALTER TABLE recruit_apply ADD CONSTRAINT uk_recruit_user UNIQUE (recruit_id, user_id);
