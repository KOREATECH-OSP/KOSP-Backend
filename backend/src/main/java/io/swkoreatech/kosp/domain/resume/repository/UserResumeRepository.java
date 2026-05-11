package io.swkoreatech.kosp.domain.resume.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.domain.resume.model.UserResume;

/**
 * 사용자 이력서 저장소.
 */
public interface UserResumeRepository extends JpaRepository<UserResume, Long> {

    /**
     * 사용자의 기본 이력서를 조회한다.
     */
    Optional<UserResume> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * 사용자의 모든 이력서를 최신 수정순으로 조회한다.
     */
    List<UserResume> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    /**
     * 특정 사용자 소유의 특정 이력서를 조회한다 (소유권 검증 포함).
     */
    Optional<UserResume> findByIdAndUserId(Long id, Long userId);

    /**
     * 사용자의 이력서 수를 반환한다.
     */
    long countByUserId(Long userId);

    /**
     * 사용자의 모든 이력서에서 기본 이력서 설정을 해제한다.
     */
    @Modifying
    @Query("UPDATE UserResume r SET r.isDefault = false WHERE r.user.id = :userId")
    void clearDefaultByUserId(@Param("userId") Long userId);
}
