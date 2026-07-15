package io.swkoreatech.kosp.domain.follow.repository;

import java.util.List;

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

    /** userId 를 팔로우하는 사람 목록 (팔로워). follower User 페치. */
    @Query("SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.following.id = :userId ORDER BY f.createdAt DESC")
    List<Follow> findFollowersByUserId(@Param("userId") Long userId);

    /** userId 가 팔로우하는 사람 목록 (팔로잉). following User 페치. */
    @Query("SELECT f FROM Follow f JOIN FETCH f.following WHERE f.follower.id = :userId ORDER BY f.createdAt DESC")
    List<Follow> findFollowingByUserId(@Param("userId") Long userId);
}
