package io.swkoreatech.kosp.domain.resume.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.swkoreatech.kosp.domain.resume.model.UserResume;

/**
 * 사용자 이력서 저장소.
 */
public interface UserResumeRepository extends JpaRepository<UserResume, Long> {

    /**
     * 사용자 ID로 이력서를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 이력서 (없으면 empty)
     */
    Optional<UserResume> findByUserId(Long userId);
}
