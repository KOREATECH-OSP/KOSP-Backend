package kr.ac.koreatech.sw.kosp.domain.community.comment.controller;

import java.net.URI;
import kr.ac.koreatech.sw.kosp.domain.community.comment.api.CommentApi;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.request.CommentCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentToggleLikeResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.service.CommentService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @Override
    @Permit(permitAll = true, description = "댓글 목록 조회")
    public ResponseEntity<CommentListResponse> getList(
        @AuthUser User user,
        Long articleId,
        Pageable pageable
    ) {
        CommentListResponse response = commentService.getList(articleId, pageable, user);
        return ResponseEntity.ok(response);
    }

    @Override
    @Permit(name = "comment:create", description = "댓글 작성")
    public ResponseEntity<Void> create(
        @AuthUser User user,
        Long articleId,
        CommentCreateRequest request
    ) {
        Long id = commentService.create(user, articleId, request);
        return ResponseEntity.created(URI.create("/community/articles/" + articleId + "/comments/" + id)).build();
    }

    @Override
    @Permit(name = "comment:delete", description = "댓글 삭제")
    public ResponseEntity<Void> delete(
        @AuthUser User user,
        Long articleId,
        Long commentId
    ) {
        commentService.delete(user, commentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "comment:like", description = "댓글 좋아요")
    public ResponseEntity<CommentToggleLikeResponse> toggleLike(
        @AuthUser User user,
        Long articleId,
        Long commentId
    ) {
        boolean isLiked = commentService.toggleLike(user, commentId);
        return ResponseEntity.ok(new CommentToggleLikeResponse(isLiked));
    }
}
