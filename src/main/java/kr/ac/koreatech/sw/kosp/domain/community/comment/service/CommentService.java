package kr.ac.koreatech.sw.kosp.domain.community.comment.service;

import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.request.CommentCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.model.Comment;
import kr.ac.koreatech.sw.kosp.domain.community.comment.model.CommentLike;
import kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentLikeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public Long create(User user, Long articleId, CommentCreateRequest req) {
        Article article = articleRepository.getById(articleId);
        Comment comment = Comment.builder()
            .author(user)
            .article(article)
            .content(req.content())
            .build();
        return commentRepository.save(comment).getId();
    }

    @Transactional
    public void delete(User user, Long commentId) {
        Comment comment = commentRepository.getById(commentId);
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
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
