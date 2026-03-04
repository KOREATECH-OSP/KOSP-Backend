-- V2__add_decision_reason.sql
-- Description: Add decision_reason column to recruit_apply table

ALTER TABLE recruit_apply 
ADD COLUMN decision_reason VARCHAR(500) NOT NULL DEFAULT '사유 미입력';

-- Backfill existing records (explicit, even with DEFAULT)
UPDATE recruit_apply 
SET decision_reason = '사유 미입력' 
WHERE decision_reason IS NULL;
