-- 팀 초대 상태 관리 컬럼 추가
-- status: PENDING / ACCEPTED / REJECTED / CANCELLED / EXPIRED
-- rejection_count: 거절 누적 횟수 (3회 이상 시 재발송 차단)
ALTER TABLE team_invite
    ADD COLUMN status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    ADD COLUMN rejection_count INT          NOT NULL DEFAULT 0;

-- 기존 데이터: is_deleted=true면 CANCELLED, false면 PENDING으로 초기화
UPDATE team_invite SET status = 'CANCELLED' WHERE is_deleted = true;
