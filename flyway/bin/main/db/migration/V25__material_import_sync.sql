-- ============================================
-- Material Import & Sync (아우누리 과제/EL 반자동 수집 · 자동 동기화 · 중복 안내)
-- 정책:
--  · 학교 인증은 서버가 다루지 않는다. 브라우저 확장이 사용자의 세션에서 스크랩한
--    정규화 데이터를 POST /v1/users/me/materials/import 로 전달하면 서버는 이를 upsert 한다.
--  · upsert 멱등 기준 = (user_id, source, source_external_id).
--  · 공개 기본값은 PRIVATE(V23 유지). 자동 수집분도 비공개로 시작한다.
--  · content_hash 로 변경을 감지해 바뀐 자료만 갱신하고 last_synced_at 을 기록한다.
--  · GitHub 프로젝트와 중복 가능성은 자동 병합하지 않고 학교자료 쪽에 안내 플래그로만 남긴다.
-- ============================================

ALTER TABLE material_item ADD COLUMN source_external_id     VARCHAR(255); -- 아우누리 자료 고유키(upsert 기준)
ALTER TABLE material_item ADD COLUMN semester_order         INT;          -- 최근학기 정렬키 (year*10 + term: 1학기1/여름2/2학기3/겨울4)
ALTER TABLE material_item ADD COLUMN auto_imported          BOOLEAN      NOT NULL DEFAULT FALSE; -- 자동 수집 여부
ALTER TABLE material_item ADD COLUMN content_hash           VARCHAR(64);  -- 변경 감지용 해시(title+메타 SHA-256)
ALTER TABLE material_item ADD COLUMN last_synced_at         TIMESTAMP;    -- 마지막 동기화 시각
ALTER TABLE material_item ADD COLUMN duplicated_with_github BOOLEAN      NOT NULL DEFAULT FALSE; -- GitHub 프로젝트 중복 가능성 안내
ALTER TABLE material_item ADD COLUMN duplicate_repo_key     VARCHAR(255); -- 중복 후보 repo (owner/repo)

-- 동일 출처 자료의 중복 생성 방지 (upsert 멱등성). external_id 가 있는 행만 대상.
CREATE UNIQUE INDEX uq_material_item_external
    ON material_item (user_id, source, source_external_id)
    WHERE source_external_id IS NOT NULL;

-- 최근 학기 최상위 정렬 + 최신순 보조정렬.
CREATE INDEX idx_material_item_user_semester
    ON material_item (user_id, semester_order DESC, material_date DESC);
