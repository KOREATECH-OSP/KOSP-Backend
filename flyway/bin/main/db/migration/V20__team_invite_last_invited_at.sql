-- 팀 초대 재발송(마지막 발송) 시각 기록.
-- 거절 누적 3회 이후에는 마지막 발송 시각으로부터 24시간이 지나야 재초대 가능하도록 판정하는 데 사용한다.
ALTER TABLE team_invite
    ADD COLUMN last_invited_at TIMESTAMP;

-- 기존 초대 행은 최초 발송 시각(created_at)으로 초기화한다.
UPDATE team_invite SET last_invited_at = created_at WHERE last_invited_at IS NULL;
