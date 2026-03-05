package io.swkoreatech.kosp.domain.community.comment.controller;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.comment.api.CommentApi;
import io.swkoreatech.kosp.domain.community.comment.dto.request.CommentCreateRequest;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentListResponse;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentToggleLikeResponse;
import io.swkoreatech.kosp.domain.community.comment.service.CommentService;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 댓글 컨트롤러.
 * {@link CommentApi}를 구현하여 댓글 관련 요청을 처리한다.
 */
@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService commentService;

    /** {@inheritDoc} */
    @Override
    @Permit(permitAll = true, name = "comments:list", description = "댓글 목록 조회")
    public ResponseEntity<CommentListResponse> getList(
        @AuthUser User user,
        Long articleId,
        Pageable pageable
    ) {
        CommentListResponse response = commentService.getList(articleId, pageable, user);
        return ResponseEntity.ok(response);
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "comment:create", description = "댓글 작성")
    public ResponseEntity<Void> create(
        @AuthUser User user,
        Long articleId,
        CommentCreateRequest request
    ) {
        Long id = commentService.create(user, articleId, request);
        return ResponseEntity.created(URI.create("/v1/community/articles/" + articleId + "/comments/" + id)).build();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    @Permit(name = "comment:like", description = "댓글 좋아요")
    public ResponseEntity<CommentToggleLikeResponse> toggleLike(
        @AuthUser User user,
        Long articleId,
        Long commentId
    ) {
        return ResponseEntity.ok(commentService.toggleLike(user, commentId));
    }
}
