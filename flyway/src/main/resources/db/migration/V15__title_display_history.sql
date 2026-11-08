-- 대표 칭호 설정 이력 추적용 컬럼 추가
-- 보조 칭호 정렬 기준: last_displayed_at DESC (NULL 후순위) → granted_at DESC
ALTER TABLE user_title
    ADD COLUMN last_displayed_at TIMESTAMP NULL DEFAULT NULL;
