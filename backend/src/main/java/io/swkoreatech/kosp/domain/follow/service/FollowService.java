package io.swkoreatech.kosp.domain.follow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowSummaryResponse;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowUserResponse;
import io.swkoreatech.kosp.domain.follow.model.Follow;
import io.swkoreatech.kosp.domain.follow.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 팔로우 서비스.
 *
 * <p>사용자 간 단방향 팔로우/언팔로우와 팔로워·팔로잉 목록, 요약 정보를 제공한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    /**
     * 대상 사용자를 팔로우한다. 이미 팔로우 중이거나 자기 자신이면 무시/거부한다.
     */
    @Transactional
    public void follow(User me, Long targetUserId) {
        if (me.getId().equals(targetUserId)) {
            throw new GlobalException(ExceptionMessage.BAD_REQUEST);
        }
        User target = userRepository.getById(targetUserId);

        if (followRepository.existsByFollowerIdAndFollowingId(me.getId(), target.getId())) {
            return; // 멱등 처리
        }
        followRepository.save(Follow.builder().follower(me).following(target).build());
        log.info("팔로우: followerId={}, followingId={}", me.getId(), targetUserId);
    }

    /**
     * 대상 사용자를 언팔로우한다.
     */
    @Transactional
    public void unfollow(User me, Long targetUserId) {
        followRepository.deleteByFollowerIdAndFollowingId(me.getId(), targetUserId);
        log.info("언팔로우: followerId={}, followingId={}", me.getId(), targetUserId);
    }

    /**
     * 특정 사용자의 팔로워 목록을 조회한다.
     */
    public List<FollowUserResponse> getFollowers(Long userId) {
        return followRepository.findFollowersByUserId(userId).stream()
            .map(f -> FollowUserResponse.from(f.getFollower()))
            .toList();
    }

    /**
     * 특정 사용자의 팔로잉 목록을 조회한다.
     */
    public List<FollowUserResponse> getFollowing(Long userId) {
        return followRepository.findFollowingByUserId(userId).stream()
            .map(f -> FollowUserResponse.from(f.getFollowing()))
            .toList();
    }

    /**
     * 특정 사용자의 팔로우 요약을 조회한다.
     * {@code viewer}가 null 이 아니면 조회자의 팔로우 여부(isFollowing)를 포함한다.
     */
    public FollowSummaryResponse getSummary(User viewer, Long userId) {
        long followerCount = followRepository.countByFollowingId(userId);
        long followingCount = followRepository.countByFollowerId(userId);
        boolean isFollowing = viewer != null
            && followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), userId);
        return new FollowSummaryResponse(followerCount, followingCount, isFollowing);
    }
}
