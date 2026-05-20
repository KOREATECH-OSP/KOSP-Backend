-- ============================================
-- V13: 칭호 시드 데이터 확장
-- COMMIT / STREAK / SEASON 계열 칭호 추가
-- ============================================

-- ===== 커밋 계열 추가 (COMMIT) =====

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('커밋 장인', 'COMMIT_100', '100번의 커밋으로 실력을 증명한 개발자', 'COMMIT', 'EPIC', TRUE, 35, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'COMMIT_COUNT_GTE', 100, '총 커밋 수 100개 이상', NOW(), NOW()
FROM title WHERE name = '커밋 장인'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('커밋 머신', 'COMMIT_300', '300번의 커밋을 쌓아올린 코드 기계', 'COMMIT', 'EPIC', TRUE, 36, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'COMMIT_COUNT_GTE', 300, '총 커밋 수 300개 이상', NOW(), NOW()
FROM title WHERE name = '커밋 머신'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('잔디밭 관리자', 'COMMIT_500', '500번의 커밋으로 GitHub를 초록으로 물들인 개발자', 'COMMIT', 'LEGENDARY', TRUE, 37, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'COMMIT_COUNT_GTE', 500, '총 커밋 수 500개 이상', NOW(), NOW()
FROM title WHERE name = '잔디밭 관리자'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('깃허브 정복자', 'COMMIT_1000', '1000번의 커밋, 전설의 경지에 오른 개발자', 'COMMIT', 'LEGENDARY', TRUE, 38, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'COMMIT_COUNT_GTE', 1000, '총 커밋 수 1000개 이상', NOW(), NOW()
FROM title WHERE name = '깃허브 정복자'
ON CONFLICT DO NOTHING;

-- ===== 꾸준함 계열 추가 (STREAK) =====

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('의지의 개발자', 'STREAK_14', '14일 연속 접속을 이어온 개발자', 'STREAK', 'RARE', TRUE, 55, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'STREAK_DAYS_GTE', 14, '연속 접속 14일 이상', NOW(), NOW()
FROM title WHERE name = '의지의 개발자'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('한 달의 수호자', 'STREAK_30', '30일 연속 접속을 유지한 개발자', 'STREAK', 'EPIC', TRUE, 56, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'STREAK_DAYS_GTE', 30, '연속 접속 30일 이상', NOW(), NOW()
FROM title WHERE name = '한 달의 수호자'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('두 달의 철인', 'STREAK_60', '60일 연속 접속, 흔들리지 않는 루틴의 소유자', 'STREAK', 'LEGENDARY', TRUE, 57, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'STREAK_DAYS_GTE', 60, '연속 접속 60일 이상', NOW(), NOW()
FROM title WHERE name = '두 달의 철인'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('전설의 개근왕', 'STREAK_100', '100일 연속 접속, KOSP 전설로 남은 개발자', 'STREAK', 'LEGENDARY', TRUE, 58, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'STREAK_DAYS_GTE', 100, '연속 접속 100일 이상', NOW(), NOW()
FROM title WHERE name = '전설의 개근왕'
ON CONFLICT DO NOTHING;

-- ===== 시즌 랭킹 계열 (SEASON) =====
-- 조건 타입: MANUAL (시즌 종료 후 관리자/배치가 수동 지급)
-- TODO: 시즌 종료 배치 로직 구현 시 SEASON_RANK_GTE 조건 타입 추가 후 자동화 예정

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 참여자', 'SEASON_PARTICIPANT', '시즌 랭킹에 참여한 개발자', 'SEASON', 'COMMON', TRUE, 200, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 참여 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '시즌 참여자'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 브론즈', 'SEASON_BRONZE', '시즌 브론즈 티어를 달성한 개발자', 'SEASON', 'COMMON', TRUE, 210, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 브론즈 달성 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '시즌 브론즈'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 실버', 'SEASON_SILVER', '시즌 실버 티어를 달성한 개발자', 'SEASON', 'RARE', TRUE, 220, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 실버 달성 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '시즌 실버'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 골드', 'SEASON_GOLD', '시즌 골드 티어를 달성한 개발자', 'SEASON', 'EPIC', TRUE, 230, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 골드 달성 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '시즌 골드'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 TOP 10', 'SEASON_TOP10', '시즌 전체 순위 TOP 10에 오른 개발자', 'SEASON', 'EPIC', TRUE, 240, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 TOP 10 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '시즌 TOP 10'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 TOP 3', 'SEASON_TOP3', '시즌 전체 순위 TOP 3에 오른 개발자', 'SEASON', 'LEGENDARY', TRUE, 250, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 TOP 3 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '시즌 TOP 3'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 1위', 'SEASON_FIRST', '시즌 최종 1위를 차지한 전설의 개발자', 'SEASON', 'LEGENDARY', TRUE, 260, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 1위 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '시즌 1위'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('시즌 성장왕', 'SEASON_GROWTH', '한 시즌 동안 가장 큰 점수 상승을 기록한 개발자', 'SEASON', 'EPIC', TRUE, 270, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '시즌 성장왕 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '시즌 성장왕'
ON CONFLICT DO NOTHING;
