-- ============================================
-- Terms: 약관 종류(type) 도입
-- ============================================
-- "활성 약관은 전체에 하나" 가정을 "활성 약관은 종류(type)마다 하나" 로 바꾼다.
-- 기존 레코드는 모두 서비스 이용약관(SERVICE) 으로 간주한다.

ALTER TABLE terms ADD COLUMN type VARCHAR(20);
UPDATE terms SET type = 'SERVICE';
ALTER TABLE terms ALTER COLUMN type SET NOT NULL;

-- 전역 UNIQUE(version) 을 (type, version) 으로 교체 (제약 이름은 V7__terms_system.sql 기준)
ALTER TABLE terms DROP CONSTRAINT uc_terms_version;
ALTER TABLE terms ADD CONSTRAINT uc_terms_type_version UNIQUE (type, version);

-- 종류당 활성 약관은 1개만 허용 (PostgreSQL 부분 유니크 인덱스)
CREATE UNIQUE INDEX uq_terms_active_per_type ON terms (type) WHERE is_active;
