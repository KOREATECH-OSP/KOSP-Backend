-- ============================================
-- V21: 시즌 티어 칭호 확장 (플래티넘/다이아/마스터/챌린저)
-- 기존 V13의 SEASON_BRONZE/SILVER/GOLD 와 함께 티어 패밀리 7종을 완성한다.
-- 조건 타입: MANUAL (일일 칭호 배치가 건드리지 않도록 함).
--   실제 지급/회수는 SeasonTierTitleService 가 현재 시즌 티어에 맞춰 자동 처리한다
--   (grant_source = SEASON_TIER, 티어 하락 시 자동 회수).
-- ============================================

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 플래티넘', 'SEASON_PLATINUM', '시즌 플래티넘 티어를 달성한 개발자', 'SEASON', 'EPIC', TRUE, 231, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 플래티넘 달성 (시즌 티어 자동 지급)', NOW(), NOW()
FROM title WHERE name = '시즌 플래티넘'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 다이아', 'SEASON_DIAMOND', '시즌 다이아 티어를 달성한 개발자', 'SEASON', 'LEGENDARY', TRUE, 232, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 다이아 달성 (시즌 티어 자동 지급)', NOW(), NOW()
FROM title WHERE name = '시즌 다이아'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 마스터', 'SEASON_MASTER', '시즌 마스터 티어에 오른 최상위 개발자', 'SEASON', 'LEGENDARY', TRUE, 233, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 마스터 달성 (시즌 티어 자동 지급)', NOW(), NOW()
FROM title WHERE name = '시즌 마스터'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 챌린저', 'SEASON_CHALLENGER', '시즌 챌린저 티어에 오른 전설의 개발자', 'SEASON', 'LEGENDARY', TRUE, 234, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 챌린저 달성 (시즌 티어 자동 지급)', NOW(), NOW()
FROM title WHERE name = '시즌 챌린저'
ON CONFLICT DO NOTHING;
