package io.swkoreatech.kosp.domain.resume.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.domain.resume.model.UserResume;
import jakarta.persistence.LockModeType;

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
     * 사용자의 모든 이력서를 비관적 쓰기 잠금과 함께 조회한다.
     *
     * <p>동시에 들어온 삭제 요청이 각자 "아직 2개 남았다"고 판단해 0개로 만드는 것을 막기 위해
     * 삭제 트랜잭션에서 이 메서드로 대상 사용자의 이력서 행 전체를 잠근다.</p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM UserResume r WHERE r.user.id = :userId ORDER BY r.id ASC")
    List<UserResume> findAllByUserIdForUpdate(@Param("userId") Long userId);

    /**
     * 사용자의 모든 이력서에서 기본 이력서 설정을 해제한다.
     */
    @Modifying
    @Query("UPDATE UserResume r SET r.isDefault = false WHERE r.user.id = :userId")
    void clearDefaultByUserId(@Param("userId") Long userId);
}
