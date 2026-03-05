package io.swkoreatech.kosp.domain.user.api;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleListResponse;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentListResponse;
import io.swkoreatech.kosp.domain.user.dto.response.GithubActivityResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 사용자 활동 조회 API.
 * 사용자의 게시글, 즐겨찾기, 댓글, GitHub 활동 등의 조회 기능을 정의한다.
 */
@Tag(name = "User Activity", description = "사용자 활동 조회 API")
@RequestMapping("/v1/users")
public interface UserActivityApi {

    /**
     * 사용자가 작성한 게시글 목록을 조회한다.
     *
     * @param user 인증된 사용자 (좋아요/즐겨찾기 여부 확인용)
     * @param userId 조회 대상 사용자 ID
     * @param pageable 페이지 정보
     * @return 게시글 목록 응답
     */
    @Operation(summary = "사용자 작성 글 목록", description = "사용자가 작성한 게시글 목록을 조회합니다.")
    @GetMapping("/{userId}/posts")
    ResponseEntity<ArticleListResponse> getPosts(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId,
        @Parameter(hidden = true) Pageable pageable
    );

    /**
     * 사용자가 즐겨찾기한 게시글 목록을 조회한다.
     *
     * @param user 인증된 사용자 (좋아요/즐겨찾기 여부 확인용)
     * @param userId 조회 대상 사용자 ID
     * @param pageable 페이지 정보
     * @return 즐겨찾기 게시글 목록 응답
     */
    @Operation(summary = "사용자 즐겨찾기 목록", description = "사용자가 즐겨찾기한 게시글 목록을 조회합니다.")
    @GetMapping("/{userId}/bookmarks")
    ResponseEntity<ArticleListResponse> getBookmarks(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId,
        @Parameter(hidden = true) Pageable pageable
    );

    /**
     * 사용자가 작성한 댓글 목록을 조회한다.
     *
     * @param user 인증된 사용자 (좋아요 여부 확인용)
     * @param userId 조회 대상 사용자 ID
     * @param pageable 페이지 정보
     * @return 댓글 목록 응답
     */
    @Operation(summary = "사용자 작성 댓글 목록", description = "사용자가 작성한 댓글 목록을 조회합니다.")
    @GetMapping("/{userId}/comments")
    ResponseEntity<CommentListResponse> getComments(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId,
        @Parameter(hidden = true) Pageable pageable
    );

    /**
     * 사용자의 GitHub 활동 내역을 조회한다.
     *
     * @param user 인증된 사용자
     * @param userId 조회 대상 사용자 ID
     * @param pageable 페이지 정보
     * @return GitHub 활동 응답
     */
    @Operation(
        summary = "사용자 활동 조회 (GitHub)",
        description = "사용자의 GitHub 활동 내역(레포지토리 등)을 조회합니다."
    )
    @GetMapping("/{userId}/activities/github")
    ResponseEntity<GithubActivityResponse> getGithubActivities(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId,
        @Parameter(hidden = true) Pageable pageable
    );
}
