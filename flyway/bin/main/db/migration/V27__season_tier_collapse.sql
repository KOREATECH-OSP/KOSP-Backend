-- ============================================
-- 시즌 티어 7단계 축소 (백분위 기반)
-- 기존 25단계(BRONZE_4~DIAMOND_1, MASTER_1~4, CHALLENGER)를
-- 7단계(BRONZE/SILVER/GOLD/PLATINUM/DIAMOND/MASTER/CHALLENGER)로 collapse.
--
-- 기본 5티어는 백분위(상위 20/40/60/80%)로, 배치에서 재배정된다.
-- MASTER/CHALLENGER는 다이아(상위 20%)이면서 총점 40/60 이상일 때만.
--
-- SeasonTier 는 @Enumerated(STRING) 이라, 기존 세부단계 문자열이 남아 있으면
-- 애플리케이션 로드 시 enum 파싱 오류가 나므로 기존 데이터를 반드시 리매핑한다.
-- (다음 배치가 티어를 재계산하지만, 그 전에 읽히는 순간을 대비해 마이그레이션에서 정리)
-- ============================================

-- 티어 매핑 함수 대용: 접두사 기준 CASE
UPDATE season_ranking_score
SET tier = CASE
    WHEN tier LIKE 'BRONZE%'     THEN 'BRONZE'
    WHEN tier LIKE 'SILVER%'     THEN 'SILVER'
    WHEN tier LIKE 'GOLD%'       THEN 'GOLD'
    WHEN tier LIKE 'PLATINUM%'   THEN 'PLATINUM'
    WHEN tier LIKE 'DIAMOND%'    THEN 'DIAMOND'
    WHEN tier LIKE 'MASTER%'     THEN 'MASTER'
    WHEN tier LIKE 'CHALLENGER%' THEN 'CHALLENGER'
    ELSE 'BRONZE'
END;

-- 기본값도 BRONZE 로 변경 (신규 insert 시 세부단계 문자열이 들어가지 않도록)
ALTER TABLE season_ranking_score ALTER COLUMN tier SET DEFAULT 'BRONZE';

-- 확정 시즌 스냅샷의 최종 티어도 동일하게 리매핑
UPDATE season_ranking_snapshot
SET final_tier = CASE
    WHEN final_tier LIKE 'BRONZE%'     THEN 'BRONZE'
    WHEN final_tier LIKE 'SILVER%'     THEN 'SILVER'
    WHEN final_tier LIKE 'GOLD%'       THEN 'GOLD'
    WHEN final_tier LIKE 'PLATINUM%'   THEN 'PLATINUM'
    WHEN final_tier LIKE 'DIAMOND%'    THEN 'DIAMOND'
    WHEN final_tier LIKE 'MASTER%'     THEN 'MASTER'
    WHEN final_tier LIKE 'CHALLENGER%' THEN 'CHALLENGER'
    ELSE 'BRONZE'
END;
