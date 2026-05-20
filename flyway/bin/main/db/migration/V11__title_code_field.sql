-- ============================================
-- V11: Title 코드 필드 추가
-- 이미지 매핑을 name 대신 code(영문 슬러그)로 참조하기 위한 필드
-- ============================================

-- 1단계: nullable로 추가
ALTER TABLE title ADD COLUMN code VARCHAR(50);

-- 2단계: 기존 칭호에 코드 채우기
UPDATE title SET code = 'first-commit'        WHERE name = '첫 줄의 개척자';
UPDATE title SET code = 'commit-habit'        WHERE name = '저장의 습관가';
UPDATE title SET code = 'commit-architect'    WHERE name = '기록의 설계자';
UPDATE title SET code = 'streak-keeper'       WHERE name = '리듬을 지키는 자';
UPDATE title SET code = 'routine-guardian'    WHERE name = '루틴의 수호자';
UPDATE title SET code = 'challenge-vanguard'  WHERE name = '도전의 선봉장';
UPDATE title SET code = 'completion-tracker'  WHERE name = '완주의 추적자';
UPDATE title SET code = 'knowledge-messenger' WHERE name = '지식의 전달자';
UPDATE title SET code = 'consensus-architect' WHERE name = '합의의 설계자';

-- 2.5단계: code가 아직 NULL인 title은 id 기반으로 자동 생성 (안전망)
-- 위 9개 UPDATE에 해당하지 않는 extra 칭호가 있을 경우 NOT NULL 실패 방지
UPDATE title SET code = 'title-' || id WHERE code IS NULL;

-- 3단계: NOT NULL + UNIQUE 제약
ALTER TABLE title ALTER COLUMN code SET NOT NULL;
ALTER TABLE title ADD CONSTRAINT uq_title_code UNIQUE (code);
