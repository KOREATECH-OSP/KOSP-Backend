-- V7: Challenge SpEL 단순화 - condition 하나로 0~100 반환
-- 기존: condition (Boolean), progressField, maxProgress
-- 변경: condition (0~100 반환하는 SpEL)

-- 1. challenge 테이블에서 불필요한 컬럼 삭제
-- 기존 condition은 그대로 두되, progressField와 maxProgress를 condition으로 통합 필요
-- 관리자가 기존 챌린지들의 condition을 새 형식으로 업데이트해야 함
ALTER TABLE challenge 
  DROP COLUMN max_progress,
  DROP COLUMN progress_field;

-- 2. challenge_history 테이블 변경
-- current_progress, target_progress → progress_at_achievement
ALTER TABLE challenge_history
  ADD COLUMN progress_at_achievement INT DEFAULT 100 AFTER achieved_at;

-- 기존 데이터 마이그레이션: 달성된 경우 100으로 설정
UPDATE challenge_history 
SET progress_at_achievement = 100 
WHERE is_achieved = true;

-- 미달성인 경우 current_progress 비율 계산
UPDATE challenge_history 
SET progress_at_achievement = LEAST(COALESCE(current_progress, 0) * 100 / NULLIF(target_progress, 0), 100)
WHERE is_achieved = false AND target_progress IS NOT NULL AND target_progress > 0;

-- 기존 컬럼 삭제
ALTER TABLE challenge_history
  DROP COLUMN current_progress,
  DROP COLUMN target_progress;
