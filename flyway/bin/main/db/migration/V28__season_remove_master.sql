-- ============================================
-- 시즌 티어 'MASTER' 제거 (7단계 → 6단계)
--
-- 티어 산정 구조에서 MASTER를 폐지한다. 기존 MASTER(다이아 상위20% & 총점 40~60)
-- 구간은 이제 DIAMOND로 흡수된다. 엘리트는 CHALLENGER(총점 60+) 단일.
--
-- SeasonTier 는 @Enumerated(STRING) 이라 DB에 남은 'MASTER' 문자열이 있으면
-- 애플리케이션 로드/조회 시 enum 파싱 오류가 나므로 반드시 DIAMOND로 리매핑한다.
-- ============================================

-- 현재 시즌 점수 티어: MASTER → DIAMOND
UPDATE season_ranking_score
SET tier = 'DIAMOND'
WHERE tier = 'MASTER';

-- 확정 시즌 스냅샷 최종 티어: MASTER → DIAMOND
UPDATE season_ranking_snapshot
SET final_tier = 'DIAMOND'
WHERE final_tier = 'MASTER';

-- 자동 지급된 '시즌 마스터'(SEASON_MASTER) 칭호를 시스템 회수 처리한다.
-- (칭호 매핑에서 MASTER 패밀리가 사라져 배치 자동 회수 대상에서 빠지므로 여기서 정리)
UPDATE user_title
SET is_revoked = TRUE, revoked_at = NOW(), updated_at = NOW()
WHERE is_revoked = FALSE
  AND title_id IN (SELECT id FROM title WHERE code = 'SEASON_MASTER');

-- SEASON_MASTER 칭호 자체를 비활성화한다 (행은 유지 — user_title FK 보존).
UPDATE title
SET is_active = FALSE, updated_at = NOW()
WHERE code = 'SEASON_MASTER';
