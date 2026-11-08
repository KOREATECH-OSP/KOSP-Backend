package io.swkoreatech.kosp.domain.follow.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowSummaryResponse;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowUserListResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

/**
 * 팔로우 API.
 * 사용자 간 팔로우/언팔로우 및 팔로워·팔로잉 목록/요약을 제공한다.
 */
@Tag(name = "Follow", description = "팔로잉/팔로워 API")
public interface FollowApi {

    @Operation(summary = "팔로우", description = "대상 사용자를 팔로우합니다. 자기 자신은 팔로우할 수 없습니다.")
    @ApiResponse(responseCode = "204", description = "팔로우 성공")
    @PostMapping("/v1/users/{userId}/follow")
    ResponseEntity<Void> follow(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId
    );

    @Operation(summary = "언팔로우", description = "대상 사용자 팔로우를 취소합니다.")
    @ApiResponse(responseCode = "204", description = "언팔로우 성공")
    @DeleteMapping("/v1/users/{userId}/follow")
    ResponseEntity<Void> unfollow(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId
    );

    @Operation(
        summary = "팔로워 목록 (페이지)",
        description = "특정 사용자를 팔로우하는 사람 목록을 최신 팔로우순으로 반환합니다. "
            + "각 항목에는 대표 칭호와, 로그인 조회자 기준 팔로우 여부(isFollowing)·본인 여부(isMe)가 포함됩니다."
    )
    @GetMapping("/v1/users/{userId}/followers")
    ResponseEntity<FollowUserListResponse> getFollowers(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId,
        @Parameter(description = "0-based 페이지 번호") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "20") int size
    );

    @Operation(
        summary = "팔로잉 목록 (페이지)",
        description = "특정 사용자가 팔로우하는 사람 목록을 최신 팔로우순으로 반환합니다. "
            + "각 항목에는 대표 칭호와, 로그인 조회자 기준 팔로우 여부(isFollowing)·본인 여부(isMe)가 포함됩니다."
    )
    @GetMapping("/v1/users/{userId}/following")
    ResponseEntity<FollowUserListResponse> getFollowing(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId,
        @Parameter(description = "0-based 페이지 번호") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "팔로우 요약", description = "팔로워/팔로잉 수와 조회자의 팔로우 여부(isFollowing)를 반환합니다.")
    @GetMapping("/v1/users/{userId}/follow-summary")
    ResponseEntity<FollowSummaryResponse> getSummary(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId
    );
}
