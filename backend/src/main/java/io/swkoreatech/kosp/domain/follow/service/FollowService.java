package io.swkoreatech.kosp.domain.follow.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.title.model.UserTitle;
import io.swkoreatech.kosp.common.title.repository.UserTitleRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowSummaryResponse;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowUserListResponse;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowUserResponse;
import io.swkoreatech.kosp.domain.follow.model.Follow;
import io.swkoreatech.kosp.domain.follow.repository.FollowRepository;
import io.swkoreatech.kosp.global.dto.PageMeta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 팔로우 서비스.
 *
 * <p>사용자 간 단방향 팔로우/언팔로우와 팔로워·팔로잉 목록, 요약 정보를 제공한다.
 * 목록은 페이지 단위로 반환하며, 각 항목에 대표 칭호와 조회자 기준 팔로우 여부를 함께 담는다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    /** 목록 페이지 기본 크기. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 목록 페이지 최대 크기 (과도한 조회 방지). */
    private static final int MAX_PAGE_SIZE = 100;

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserTitleRepository userTitleRepository;

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
     * 특정 사용자의 팔로워 목록 페이지를 조회한다 (해당 사용자를 팔로우하는 사람).
     *
     * @param viewer 조회자 (비로그인이면 {@code null})
     * @param userId 목록 주인의 사용자 ID
     * @param page   0-based 페이지 번호
     * @param size   페이지 크기
     */
    public FollowUserListResponse getFollowers(User viewer, Long userId, int page, int size) {
        Page<Follow> result = followRepository.findFollowerPage(userId, toPageable(page, size));
        return toListResponse(viewer, result, Follow::getFollower);
    }

    /**
     * 특정 사용자의 팔로잉 목록 페이지를 조회한다 (해당 사용자가 팔로우하는 사람).
     *
     * @param viewer 조회자 (비로그인이면 {@code null})
     * @param userId 목록 주인의 사용자 ID
     * @param page   0-based 페이지 번호
     * @param size   페이지 크기
     */
    public FollowUserListResponse getFollowing(User viewer, Long userId, int page, int size) {
        Page<Follow> result = followRepository.findFollowingPage(userId, toPageable(page, size));
        return toListResponse(viewer, result, Follow::getFollowing);
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

    // ── private helpers ──────────────────────────────────────────────

    private PageRequest toPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }

    /**
     * 팔로우 페이지를 응답 DTO 로 변환한다.
     * 대표 칭호와 조회자 기준 팔로우 여부는 페이지 단위로 일괄 조회해 N+1 을 피한다.
     *
     * @param extractor {@link Follow} 에서 목록에 표시할 사용자를 꺼내는 함수
     */
    private FollowUserListResponse toListResponse(
        User viewer,
        Page<Follow> page,
        Function<Follow, User> extractor
    ) {
        List<User> users = page.getContent().stream().map(extractor).toList();
        if (users.isEmpty()) {
            return new FollowUserListResponse(List.of(), PageMeta.from(page));
        }

        List<Long> userIds = users.stream().map(User::getId).toList();
        Map<Long, UserTitle> displayTitles = userTitleRepository.findAllDisplayTitlesByUserIds(userIds).stream()
            .collect(Collectors.toMap(
                ut -> ut.getUser().getId(),
                Function.identity(),
                (left, right) -> left
            ));

        Long viewerId = viewer == null ? null : viewer.getId();
        Set<Long> followingIds = viewerId == null
            ? Collections.emptySet()
            : new HashSet<>(followRepository.findFollowingIdsIn(viewerId, userIds));

        List<FollowUserResponse> content = users.stream()
            .map(u -> FollowUserResponse.of(
                u,
                displayTitles.get(u.getId()),
                followingIds.contains(u.getId()),
                u.getId().equals(viewerId)
            ))
            .toList();

        return new FollowUserListResponse(content, PageMeta.from(page));
    }
}
