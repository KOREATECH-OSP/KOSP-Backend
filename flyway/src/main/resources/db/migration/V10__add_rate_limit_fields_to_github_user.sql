-- V10__add_rate_limit_fields_to_github_user.sql
-- Description: Add rate limit reset time tracking to github_user table

ALTER TABLE github_user 
ADD COLUMN rate_limit_reset_at TIMESTAMP(6) NULL;

CREATE INDEX idx_github_user_rate_limit_reset_at ON github_user (rate_limit_reset_at);
