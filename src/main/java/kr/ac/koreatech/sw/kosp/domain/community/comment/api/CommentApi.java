package kr.ac.koreatech.sw.kosp.domain.community.comment.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.request.CommentCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentToggleLikeResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Comment", description = "댓글 관리 API")
public interface CommentApi {

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 조회합니다.")
    @GetMapping("/community/articles/{articleId}/comments")
    ResponseEntity<CommentListResponse> getList(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long articleId,
        @Parameter(hidden = true) Pageable pageable
    );

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @PostMapping("/community/articles/{articleId}/comments")
    ResponseEntity<Void> create(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long articleId,
        @RequestBody @Valid CommentCreateRequest request
    );

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/community/articles/{articleId}/comments/{commentId}")
    ResponseEntity<Void> delete(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long articleId,
        @PathVariable Long commentId
    );

    @Operation(summary = "댓글 좋아요", description = "댓글 좋아요를 토글합니다.")
    @PostMapping("/community/articles/{articleId}/comments/{commentId}/likes")
    ResponseEntity<CommentToggleLikeResponse> toggleLike(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long articleId,
        @PathVariable Long commentId
    );
}
