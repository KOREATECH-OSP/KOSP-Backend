-- ============================================
-- Season Ranking Seed Data
-- ============================================

-- 2026-1학기 시즌
INSERT INTO season (created_at, updated_at, name, start_date, end_date, is_active)
VALUES (NOW(), NOW(), '2026-1학기', '2026-03-02', '2026-06-30', TRUE)
ON CONFLICT (name) DO NOTHING;

-- 점수 정책 (2026-1학기)
INSERT INTO season_score_policy (created_at, updated_at, season_id, score_type, base_score, max_daily_score, extra_config)
SELECT NOW(), NOW(), s.id, 'ATTENDANCE_DAILY', 0.0100, 0.0100, NULL
FROM season s WHERE s.name = '2026-1학기'
ON CONFLICT (season_id, score_type) DO NOTHING;

INSERT INTO season_score_policy (created_at, updated_at, season_id, score_type, base_score, max_daily_score, extra_config)
SELECT NOW(), NOW(), s.id, 'COMMIT_PER_UNIT', 0.0500, 0.1500, '{"daily_cap": 3}'::JSONB
FROM season s WHERE s.name = '2026-1학기'
ON CONFLICT (season_id, score_type) DO NOTHING;

INSERT INTO season_score_policy (created_at, updated_at, season_id, score_type, base_score, max_daily_score, extra_config)
SELECT NOW(), NOW(), s.id, 'STREAK_BONUS_7', 1.0000, NULL, '{"threshold": 7}'::JSONB
FROM season s WHERE s.name = '2026-1학기'
ON CONFLICT (season_id, score_type) DO NOTHING;

INSERT INTO season_score_policy (created_at, updated_at, season_id, score_type, base_score, max_daily_score, extra_config)
SELECT NOW(), NOW(), s.id, 'STREAK_BONUS_30', 3.0000, NULL, '{"threshold": 30}'::JSONB
FROM season s WHERE s.name = '2026-1학기'
ON CONFLICT (season_id, score_type) DO NOTHING;

INSERT INTO season_score_policy (created_at, updated_at, season_id, score_type, base_score, max_daily_score, extra_config)
SELECT NOW(), NOW(), s.id, 'STREAK_BONUS_50', 5.0000, NULL, '{"threshold": 50}'::JSONB
FROM season s WHERE s.name = '2026-1학기'
ON CONFLICT (season_id, score_type) DO NOTHING;

INSERT INTO season_score_policy (created_at, updated_at, season_id, score_type, base_score, max_daily_score, extra_config)
SELECT NOW(), NOW(), s.id, 'STREAK_BONUS_100', 10.0000, NULL, '{"threshold": 100}'::JSONB
FROM season s WHERE s.name = '2026-1학기'
ON CONFLICT (season_id, score_type) DO NOTHING;

INSERT INTO season_score_policy (created_at, updated_at, season_id, score_type, base_score, max_daily_score, extra_config)
SELECT NOW(), NOW(), s.id, 'COMMUNITY_ARTICLE', 0.5000, NULL, '{"max_total": 10.0}'::JSONB
FROM season s WHERE s.name = '2026-1학기'
ON CONFLICT (season_id, score_type) DO NOTHING;
