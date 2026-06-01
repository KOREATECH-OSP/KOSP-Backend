-- ============================================
-- V12: 다중 이력서 지원 + 관리자 칭호 이미지 수정 권한 추가
-- ============================================

-- ─────────────────────────────────────────────
-- 1. 관리자 칭호 이미지 수정 권한 추가
--    PermissionInitializer가 기존 AdminPolicy를 덮어쓰지 않으므로
--    신규 추가된 admin:titles:update 권한을 직접 policy에 연결한다.
-- ─────────────────────────────────────────────

-- 권한이 없으면 생성 (PermissionInitializer가 이미 만들었을 수도 있음)
INSERT INTO permission (name, description, created_at, updated_at)
VALUES ('admin:titles:update', '칭호 아이콘 URL 수정', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- AdminPolicy에 해당 권한이 없으면 연결
INSERT INTO policy_permission (policy_id, permission_id)
SELECT p.id, perm.id
FROM policy p
         CROSS JOIN permission perm
WHERE p.name = 'AdminPolicy'
  AND perm.name = 'admin:titles:update'
ON CONFLICT DO NOTHING;


-- ─────────────────────────────────────────────
-- 2. 다중 이력서 지원
--    기존: user_resume.user_id UNIQUE (1인 1개)
--    변경: UNIQUE 제약 제거 + is_default 컬럼 추가
-- ─────────────────────────────────────────────

-- 2-1. UNIQUE 제약 제거
ALTER TABLE user_resume DROP CONSTRAINT IF EXISTS uc_user_resume_user;

-- 2-2. is_default 컬럼 추가 (기본값 FALSE)
ALTER TABLE user_resume ADD COLUMN IF NOT EXISTS is_default BOOLEAN NOT NULL DEFAULT FALSE;

-- 2-3. 기존 이력서는 기본 이력서로 마이그레이션
UPDATE user_resume SET is_default = TRUE WHERE is_default = FALSE;

-- 2-4. 조회 성능을 위한 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_user_resume_user_default ON user_resume (user_id, is_default);
