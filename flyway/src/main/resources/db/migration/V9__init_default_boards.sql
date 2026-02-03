-- Initialize default boards with idempotent INSERT statements
INSERT INTO board (name, description, is_recruit_allowed, is_notice, created_at, updated_at)
SELECT '자유게시판', '자유롭게 이야기하는 공간', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM board WHERE name = '자유게시판');

INSERT INTO board (name, description, is_recruit_allowed, is_notice, created_at, updated_at)
SELECT '정보공유', '개발 정보를 공유합니다', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM board WHERE name = '정보공유');

INSERT INTO board (name, description, is_recruit_allowed, is_notice, created_at, updated_at)
SELECT '공지사항', '중요한 공지사항입니다', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM board WHERE name = '공지사항');

INSERT INTO board (name, description, is_recruit_allowed, is_notice, created_at, updated_at)
SELECT '모집공고', '팀원 모집공고 게시판', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM board WHERE name = '모집공고');
