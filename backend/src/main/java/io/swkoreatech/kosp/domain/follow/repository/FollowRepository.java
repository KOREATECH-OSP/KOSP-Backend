package io.swkoreatech.kosp.domain.follow.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.domain.follow.model.Follow;

/**
 * 팔로우 관계 저장소.
 */
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /** follower 가 following 을 이미 팔로우하는지 여부. */
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /** 팔로우 관계 삭제 (언팔로우). */
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /** userId 를 팔로우하는 사람 수 (팔로워 수). */
    long countByFollowingId(Long followingId);

    /** userId 가 팔로우하는 사람 수 (팔로잉 수). */
    long countByFollowerId(Long followerId);

    /**
     * userId 를 팔로우하는 사람 목록 페이지 (팔로워).
     * follower 와 그 GitHub 계정을 함께 페치해 목록 렌더링 시 N+1 을 피한다.
     */
    @Query(
        value = "SELECT f FROM Follow f "
            + "JOIN FETCH f.follower u LEFT JOIN FETCH u.githubUser "
            + "WHERE f.following.id = :userId ORDER BY f.createdAt DESC, f.id DESC",
        countQuery = "SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId"
    )
    Page<Follow> findFollowerPage(@Param("userId") Long userId, Pageable pageable);

    /**
     * userId 가 팔로우하는 사람 목록 페이지 (팔로잉).
     * following 과 그 GitHub 계정을 함께 페치해 목록 렌더링 시 N+1 을 피한다.
     */
    @Query(
        value = "SELECT f FROM Follow f "
            + "JOIN FETCH f.following u LEFT JOIN FETCH u.githubUser "
            + "WHERE f.follower.id = :userId ORDER BY f.createdAt DESC, f.id DESC",
        countQuery = "SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId"
    )
    Page<Follow> findFollowingPage(@Param("userId") Long userId, Pageable pageable);

    /**
     * 조회자가 팔로우 중인 대상 ID 를 한 번에 조회한다 (목록 항목별 팔로우 여부 판별용).
     *
     * @param viewerId  조회자 ID
     * @param targetIds 판별 대상 사용자 ID 목록
     * @return 조회자가 이미 팔로우 중인 대상 ID 목록
     */
    @Query("SELECT f.following.id FROM Follow f "
        + "WHERE f.follower.id = :viewerId AND f.following.id IN :targetIds")
    List<Long> findFollowingIdsIn(
        @Param("viewerId") Long viewerId,
        @Param("targetIds") Collection<Long> targetIds
    );
}
