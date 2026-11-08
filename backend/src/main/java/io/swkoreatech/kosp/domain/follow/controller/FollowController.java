package io.swkoreatech.kosp.domain.follow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.follow.api.FollowApi;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowSummaryResponse;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowUserListResponse;
import io.swkoreatech.kosp.domain.follow.service.FollowService;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 팔로우 컨트롤러.
 * {@link FollowApi}의 구현체.
 */
@RestController
@RequiredArgsConstructor
public class FollowController implements FollowApi {

    private final FollowService followService;

    @Override
    @Permit(description = "팔로우")
    public ResponseEntity<Void> follow(@AuthUser User user, @PathVariable Long userId) {
        followService.follow(user, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(description = "언팔로우")
    public ResponseEntity<Void> unfollow(@AuthUser User user, @PathVariable Long userId) {
        followService.unfollow(user, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(permitAll = true, description = "팔로워 목록 조회")
    public ResponseEntity<FollowUserListResponse> getFollowers(
        @AuthUser User user, @PathVariable Long userId, int page, int size
    ) {
        return ResponseEntity.ok(followService.getFollowers(user, userId, page, size));
    }

    @Override
    @Permit(permitAll = true, description = "팔로잉 목록 조회")
    public ResponseEntity<FollowUserListResponse> getFollowing(
        @AuthUser User user, @PathVariable Long userId, int page, int size
    ) {
        return ResponseEntity.ok(followService.getFollowing(user, userId, page, size));
    }

    @Override
    @Permit(description = "팔로우 요약 조회")
    public ResponseEntity<FollowSummaryResponse> getSummary(@AuthUser User user, @PathVariable Long userId) {
        return ResponseEntity.ok(followService.getSummary(user, userId));
    }
}
