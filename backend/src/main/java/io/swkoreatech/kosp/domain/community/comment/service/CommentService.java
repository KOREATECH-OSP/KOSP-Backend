package io.swkoreatech.kosp.domain.community.comment.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.comment.dto.request.CommentCreateRequest;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentListResponse;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentResponse;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentToggleLikeResponse;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;
import io.swkoreatech.kosp.domain.community.comment.model.CommentLike;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentLikeRepository;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;

/**
 * 댓글 서비스.
 * 댓글의 CRUD 및 좋아요 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ArticleRepository articleRepository;

    /**
     * 댓글을 작성한다.
     *
     * @param user 작성자
     * @param articleId 게시글 ID
     * @param request 댓글 작성 요청
     * @return 생성된 댓글 ID
     */
    @Transactional
    public Long create(User user, Long articleId, CommentCreateRequest request) {
        Article article = articleRepository.getById(articleId);
        Comment comment = Comment.builder()
            .author(user)
            .article(article)
            .content(request.content())
            .build();
        commentRepository.save(comment);

        // Increment article comment count
        article.incrementCommentsCount();
        articleRepository.save(article);

        return comment.getId();
    }

    /**
     * 댓글을 삭제한다.
     *
     * @param user 삭제 요청 사용자
     * @param commentId 댓글 ID
     * @throws GlobalException 작성자가 아닌 경우
     */
    @Transactional
    public void delete(User user, Long commentId) {
        Comment comment = commentRepository.getById(commentId);
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }

        Article article = comment.getArticle();
        article.decrementCommentsCount();

        comment.delete();
    }

    /**
     * 게시글의 댓글 목록을 조회한다.
     *
     * @param articleId 게시글 ID
     * @param pageable 페이징 정보
     * @param user 조회하는 사용자
     * @return 댓글 목록 응답
     */
    public CommentListResponse getList(Long articleId, Pageable pageable, User user) {
        Page<Comment> page = commentRepository.findByArticleIdAndIsDeletedFalse(articleId, pageable);
        return toResponse(page, user);
    }

    private CommentListResponse toResponse(Page<Comment> page, User user) {
        List<CommentResponse> comments = page.getContent().stream()
            .map(comment -> CommentResponse.from(
                comment,
                isLiked(user, comment),
                isMine(user, comment)
            ))
            .toList();
        return CommentListResponse.from(comments, page);
    }

    private boolean isLiked(User user, Comment comment) {
        return user != null && commentLikeRepository.existsByUserAndComment(user, comment);
    }

    private boolean isMine(User user, Comment comment) {
        return user != null && comment.getAuthor().getId().equals(user.getId());
    }

    /**
     * 댓글 좋아요를 토글한다.
     *
     * @param user 사용자
     * @param commentId 댓글 ID
     * @return 좋아요 토글 응답
     */
    @Transactional
    public CommentToggleLikeResponse toggleLike(User user, Long commentId) {
        Comment comment = commentRepository.getById(commentId);
        Optional<CommentLike> like = commentLikeRepository.findByUserAndComment(user, comment);
        if (like.isPresent()) {
            commentLikeRepository.delete(like.get());
            return CommentToggleLikeResponse.from(false);
        }
        commentLikeRepository.save(CommentLike.builder().user(user).comment(comment).build());
        return CommentToggleLikeResponse.from(true);
    }
}
