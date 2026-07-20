package io.swkoreatech.kosp.domain.material.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.swkoreatech.kosp.domain.material.model.MaterialItem;
import io.swkoreatech.kosp.domain.material.model.MaterialSource;

/**
 * 학습자료 아이템 저장소.
 */
public interface MaterialItemRepository extends JpaRepository<MaterialItem, Long> {

    /**
     * 아우누리 import upsert 기준 조회. (user_id, source, source_external_id) 로 기존 자료를 찾는다.
     */
    Optional<MaterialItem> findByUserIdAndSourceAndSourceExternalId(
        Long userId, MaterialSource source, String sourceExternalId
    );

    /**
     * 특정 폴더의 자료를 최신순(material_date 우선)으로 조회한다.
     */
    List<MaterialItem> findAllByFolderIdOrderByMaterialDateDescIdDesc(Long folderId);

    /**
     * 사용자의 전체 자료를 최신순으로 조회한다 (마이페이지 최신 노출용, 페이징).
     */
    List<MaterialItem> findAllByUserIdOrderByMaterialDateDescIdDesc(Long userId, Pageable pageable);

    /**
     * 본인 소유의 특정 자료를 조회한다 (소유권 검증 포함).
     */
    Optional<MaterialItem> findByIdAndUserId(Long id, Long userId);

    /**
     * 사용자의 자동 수집 자료를 최근학기 우선·최신순으로 조회한다 (이력서 자동 프로젝트 투영용).
     */
    List<MaterialItem> findAllByUserIdAndAutoImportedTrueOrderBySemesterOrderDescMaterialDateDescIdDesc(Long userId);

    /**
     * 폴더에 속한 자료 수를 반환한다.
     */
    long countByFolderId(Long folderId);
}
