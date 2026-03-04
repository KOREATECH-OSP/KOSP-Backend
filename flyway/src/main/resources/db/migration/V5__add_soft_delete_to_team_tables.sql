-- Team 테이블에 is_deleted 컬럼 추가
ALTER TABLE team ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- TeamMember 테이블에 is_deleted 컬럼 추가
ALTER TABLE team_member ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- TeamInvite 테이블에 is_deleted 컬럼 추가
ALTER TABLE team_invite ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
