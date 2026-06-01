-- ============================================
-- V14: 출석 계열 칭호 및 커밋 계열 추가명 칭호
-- ATTENDANCE / 커밋 새 명칭 (첫 커밋러, 꾸준한 커밋러, 코드 기록자)
-- ============================================

-- ===== 출석 계열 (ATTENDANCE) =====
-- 조건 타입: MANUAL (출석 집계 배치 미구현 → 관리자 수동 지급)

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('첫 출석', 'ATTEND_1', 'KOSP에 처음 출석한 개발자', 'ATTENDANCE', 'COMMON', TRUE, 60, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 1, '출석 1회 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '첫 출석'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('성실한 참여자', 'ATTEND_5', '5회 이상 출석한 성실한 개발자', 'ATTENDANCE', 'COMMON', TRUE, 61, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 5, '출석 5회 이상 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '성실한 참여자'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('꾸준한 참여자', 'ATTEND_10', '10회 이상 출석하며 꾸준함을 증명한 개발자', 'ATTENDANCE', 'RARE', TRUE, 62, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 10, '출석 10회 이상 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '꾸준한 참여자'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('출석 우등생', 'ATTEND_20', '20회 이상 출석한 우등 개발자', 'ATTENDANCE', 'RARE', TRUE, 63, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 20, '출석 20회 이상 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '출석 우등생'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('개근 도전자', 'ATTEND_30', '30회 이상 출석을 달성한 개발자', 'ATTENDANCE', 'EPIC', TRUE, 64, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 30, '출석 30회 이상 (관리자 수동 지급 또는 배치)', NOW(), NOW()
FROM title WHERE name = '개근 도전자'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('KOSP 개근왕', 'ATTEND_ALL', '모든 활동에 빠짐없이 참석한 전설의 개근 개발자', 'ATTENDANCE', 'LEGENDARY', TRUE, 65, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'MANUAL', 0, '전체 개근 달성 (관리자 수동 지급)', NOW(), NOW()
FROM title WHERE name = 'KOSP 개근왕'
ON CONFLICT DO NOTHING;

-- ===== 커밋 계열 추가 명칭 =====
-- 기존 V4의 '첫 줄의 개척자'(1), '저장의 습관가'(10), '기록의 설계자'(50)와 별개로
-- 사용자 요청 명칭의 칭호를 추가 등록

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('첫 커밋러', 'COMMIT_FIRST', '첫 번째 커밋을 기록한 개발자', 'COMMIT', 'COMMON', TRUE, 11, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'COMMIT_COUNT_GTE', 1, '총 커밋 수 1개 이상', NOW(), NOW()
FROM title WHERE name = '첫 커밋러'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('꾸준한 커밋러', 'COMMIT_HABIT', '꾸준히 10번의 커밋을 쌓아온 개발자', 'COMMIT', 'COMMON', TRUE, 21, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'COMMIT_COUNT_GTE', 10, '총 커밋 수 10개 이상', NOW(), NOW()
FROM title WHERE name = '꾸준한 커밋러'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, code, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('코드 기록자', 'COMMIT_RECORDER', '50번의 커밋으로 코드 역사를 쌓은 개발자', 'COMMIT', 'RARE', TRUE, 31, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'COMMIT_COUNT_GTE', 50, '총 커밋 수 50개 이상', NOW(), NOW()
FROM title WHERE name = '코드 기록자'
ON CONFLICT DO NOTHING;
