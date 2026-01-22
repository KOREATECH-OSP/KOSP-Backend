-- Add is_deleted column to comment table for soft delete
ALTER TABLE comment ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index for faster queries on non-deleted comments
CREATE INDEX idx_comment_is_deleted ON comment(is_deleted);
CREATE INDEX idx_comment_article_deleted ON comment(article_id, is_deleted);
