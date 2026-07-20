package io.swkoreatech.kosp.domain.material.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.domain.material.model.MaterialFolder;
import io.swkoreatech.kosp.domain.material.model.MaterialSource;
import io.swkoreatech.kosp.domain.material.model.Visibility;

/**
 * 학습자료 폴더 저장소.
 */
public interface MaterialFolderRepository extends JpaRepository<MaterialFolder, Long> {

    /**
     * import 자동 배정용 폴더를 출처+이름으로 조회한다 (없으면 생성 대상).
     */
    Optional<MaterialFolder> findFirstByUserIdAndSourceAndName(Long userId, MaterialSource source, String name);

    /**
     * 사용자의 전체 폴더를 정렬 순서대로 조회한다 (폴더 트리 구성용).
     */
    List<MaterialFolder> findAllByUserIdOrderBySortOrderAscIdAsc(Long userId);

    /**
     * 사용자의 특정 공개 범위 폴더만 조회한다 (타인 조회용).
     */
    List<MaterialFolder> findAllByUserIdAndVisibilityOrderBySortOrderAscIdAsc(Long userId, Visibility visibility);

    /**
     * 본인 소유의 특정 폴더를 조회한다 (소유권 검증 포함).
     */
    Optional<MaterialFolder> findByIdAndUserId(Long id, Long userId);

    /**
     * 사용자의 시작 폴더를 조회한다.
     */
    Optional<MaterialFolder> findByUserIdAndIsStartFolderTrue(Long userId);

    /**
     * 사용자의 모든 폴더에서 시작 폴더 설정을 해제한다.
     */
    @Modifying
    @Query("UPDATE MaterialFolder f SET f.isStartFolder = false WHERE f.user.id = :userId")
    void clearStartFolderByUserId(@Param("userId") Long userId);
}
