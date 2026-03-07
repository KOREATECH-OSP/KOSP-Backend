package io.swkoreatech.kosp.domain.community.comment.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.comment.dto.request.CommentCreateRequest;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentListResponse;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentToggleLikeResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 댓글 API 인터페이스.
 * 댓글의 CRUD 및 좋아요 엔드포인트를 정의한다.
 */
@Tag(name = "Community - Comment", description = "댓글 관리 API")
public interface CommentApi {

    /**
     * 게시글의 댓글 목록을 조회한다.
     *
     * @param user 인증된 사용자
     * @param articleId 게시글 ID
     * @param pageable 페이징 정보
     * @return 댓글 목록 응답
     */
    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 조회합니다.")
    @GetMapping("/v1/community/articles/{articleId}/comments")
    ResponseEntity<CommentListResponse> getList(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long articleId,
        @Parameter(hidden = true) Pageable pageable
    );

    /**
     * 게시글에 댓글을 작성한다.
     *
     * @param user 인증된 사용자
     * @param articleId 게시글 ID
     * @param request 댓글 작성 요청
     * @return 생성 응답
     */
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @PostMapping("/v1/community/articles/{articleId}/comments")
    ResponseEntity<Void> create(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long articleId,
        @RequestBody @Valid CommentCreateRequest request
    );

    /**
     * 댓글을 삭제한다.
     *
     * @param user 인증된 사용자
     * @param articleId 게시글 ID
     * @param commentId 댓글 ID
     * @return 삭제 응답
     */
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/v1/community/articles/{articleId}/comments/{commentId}")
    ResponseEntity<Void> delete(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long articleId,
        @PathVariable Long commentId
    );

    /**
     * 댓글 좋아요를 토글한다.
     *
     * @param user 인증된 사용자
     * @param articleId 게시글 ID
     * @param commentId 댓글 ID
     * @return 좋아요 토글 응답
     */
    @Operation(summary = "댓글 좋아요", description = "댓글 좋아요를 토글합니다.")
    @PostMapping("/v1/community/articles/{articleId}/comments/{commentId}/likes")
    ResponseEntity<CommentToggleLikeResponse> toggleLike(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long articleId,
        @PathVariable Long commentId
    );
}
