-- Challenge 엔티티 icon 필드를 imageResource + imageResourceType으로 변경
ALTER TABLE challenge ADD COLUMN image_resource VARCHAR(255);
ALTER TABLE challenge ADD COLUMN image_resource_type VARCHAR(20);

-- 기존 icon 데이터를 imageResource로 마이그레이션 (기본값: ICON 타입)
UPDATE challenge 
SET image_resource = icon, 
    image_resource_type = 'ICON'
WHERE icon IS NOT NULL;

-- 기존 icon 컬럼 삭제
ALTER TABLE challenge DROP COLUMN icon;
