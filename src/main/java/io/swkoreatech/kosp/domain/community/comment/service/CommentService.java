package io.swkoreatech.kosp.domain.community.comment.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.comment.dto.request.CommentCreateRequest;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentListResponse;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentResponse;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;
import io.swkoreatech.kosp.domain.community.comment.model.CommentLike;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentLikeRepository;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.dto.PageMeta;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ArticleRepository articleRepository;

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

    @Transactional
    public void delete(User user, Long commentId) {
        Comment comment = commentRepository.getById(commentId);
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
        
        // Decrement article comment count
        Article article = comment.getArticle();
        article.decrementCommentsCount();
        articleRepository.save(article);
        
        commentRepository.delete(comment);
    }

    public CommentListResponse getList(Long articleId, Pageable pageable, User user) {
        Page<Comment> page = commentRepository.findByArticleId(articleId, pageable);
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
        return new CommentListResponse(comments, PageMeta.from(page));
    }

    private boolean isLiked(User user, Comment comment) {
        return user != null && commentLikeRepository.existsByUserAndComment(user, comment);
    }

    private boolean isMine(User user, Comment comment) {
        return user != null && comment.getAuthor().getId().equals(user.getId());
    }

    @Transactional
    public boolean toggleLike(User user, Long commentId) {
        Comment comment = commentRepository.getById(commentId);
        Optional<CommentLike> like = commentLikeRepository.findByUserAndComment(user, comment);
        if (like.isPresent()) {
            commentLikeRepository.delete(like.get());
            return false;
        }
        commentLikeRepository.save(CommentLike.builder().user(user).comment(comment).build());
        return true;
    }
}
