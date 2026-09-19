-- organizations 테이블에 조직 설명 및 태그 컬럼 추가
ALTER TABLE organizations
    ADD COLUMN description TEXT,
    ADD COLUMN tags        VARCHAR(500);
