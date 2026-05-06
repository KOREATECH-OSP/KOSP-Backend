-- ============================================
-- Terms Seed Data
-- ============================================

-- 초기 약관 버전 삽입 (버전 1.0)
INSERT INTO terms (created_at, updated_at, version, content, is_active)
VALUES (
    NOW(),
    NOW(),
    '1.0',
    '서비스 이용약관 (v1.0)\n\n본 약관은 K-OSP 서비스 이용에 관한 기본적인 사항을 규정합니다.\n추후 실제 약관 내용으로 대체 예정.',
    TRUE
);
