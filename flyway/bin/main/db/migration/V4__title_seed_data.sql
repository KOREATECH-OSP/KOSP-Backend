-- ============================================
-- Title System Seed Data
-- 1차 MVP 기본 칭호 10종
-- ============================================

-- ===== 커밋 계열 (COMMIT) =====
INSERT INTO title (name, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('첫 줄의 개척자', '첫 번째 커밋을 완료한 개발자', 'COMMIT', 'COMMON', TRUE, 10, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'COMMIT_COUNT_GTE', 1, '총 커밋 수 1개 이상', NOW(), NOW()
FROM title WHERE name = '첫 줄의 개척자'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('저장의 습관가', '꾸준히 10번의 커밋을 쌓아온 개발자', 'COMMIT', 'COMMON', TRUE, 20, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'COMMIT_COUNT_GTE', 10, '총 커밋 수 10개 이상', NOW(), NOW()
FROM title WHERE name = '저장의 습관가'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('기록의 설계자', '50번의 커밋으로 코드 역사를 쌓은 개발자', 'COMMIT', 'RARE', TRUE, 30, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'COMMIT_COUNT_GTE', 50, '총 커밋 수 50개 이상', NOW(), NOW()
FROM title WHERE name = '기록의 설계자'
ON CONFLICT DO NOTHING;

-- ===== 꾸준함 계열 (STREAK) =====
INSERT INTO title (name, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('리듬을 지키는 자', '3일 연속 접속을 유지한 개발자', 'STREAK', 'COMMON', TRUE, 40, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'STREAK_DAYS_GTE', 3, '연속 접속 3일 이상', NOW(), NOW()
FROM title WHERE name = '리듬을 지키는 자'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('루틴의 수호자', '7일 연속 접속을 이어온 개발자', 'STREAK', 'RARE', TRUE, 50, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'STREAK_DAYS_GTE', 7, '연속 접속 7일 이상', NOW(), NOW()
FROM title WHERE name = '루틴의 수호자'
ON CONFLICT DO NOTHING;

-- ===== 챌린지 계열 (CHALLENGE) =====
INSERT INTO title (name, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('도전의 선봉장', '첫 번째 챌린지를 완료한 개발자', 'CHALLENGE', 'COMMON', TRUE, 60, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'CHALLENGE_COUNT_GTE', 1, '챌린지 완료 1개 이상', NOW(), NOW()
FROM title WHERE name = '도전의 선봉장'
ON CONFLICT DO NOTHING;

INSERT INTO title (name, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('완주의 추적자', '3개의 챌린지를 완료한 개발자', 'CHALLENGE', 'RARE', TRUE, 70, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'CHALLENGE_COUNT_GTE', 3, '챌린지 완료 3개 이상', NOW(), NOW()
FROM title WHERE name = '완주의 추적자'
ON CONFLICT DO NOTHING;

-- ===== 커뮤니티 계열 (COMMUNITY) =====
-- 지식의 전달자: ARTICLE_COUNT_GTE 조건 등록 (2차에 자동 지급 고도화 예정)
INSERT INTO title (name, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('지식의 전달자', '첫 번째 게시글을 작성한 개발자', 'COMMUNITY', 'COMMON', TRUE, 80, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'ARTICLE_COUNT_GTE', 1, '게시글 작성 1개 이상', NOW(), NOW()
FROM title WHERE name = '지식의 전달자'
ON CONFLICT DO NOTHING;

-- ===== 협업 계열 (COLLABORATION) =====
-- 합의의 설계자: TEAM_JOIN_COUNT_GTE 조건 등록 (2차에 자동 지급 고도화 예정)
INSERT INTO title (name, description, category, rarity, is_active, display_order, created_at, updated_at)
VALUES ('합의의 설계자', '팀에 참여한 개발자', 'COLLABORATION', 'COMMON', TRUE, 90, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO title_condition (title_id, condition_type, threshold_value, description, created_at, updated_at)
SELECT id, 'TEAM_JOIN_COUNT_GTE', 1, '팀 참여 1회 이상', NOW(), NOW()
FROM title WHERE name = '합의의 설계자'
ON CONFLICT DO NOTHING;
