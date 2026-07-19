package io.swkoreatech.kosp.domain.follow.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.follow.api.FollowApi;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowSummaryResponse;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowUserResponse;
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
    public ResponseEntity<List<FollowUserResponse>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    @Override
    @Permit(permitAll = true, description = "팔로잉 목록 조회")
    public ResponseEntity<List<FollowUserResponse>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    @Override
    @Permit(description = "팔로우 요약 조회")
    public ResponseEntity<FollowSummaryResponse> getSummary(@AuthUser User user, @PathVariable Long userId) {
        return ResponseEntity.ok(followService.getSummary(user, userId));
    }
}
