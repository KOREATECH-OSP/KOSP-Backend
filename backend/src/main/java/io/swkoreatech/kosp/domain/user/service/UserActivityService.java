package io.swkoreatech.kosp.domain.user.service;


import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleListResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleResponse;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleLikeRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentListResponse;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentResponse;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentLikeRepository;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentRepository;
import io.swkoreatech.kosp.domain.user.dto.response.GithubActivityResponse;
import io.swkoreatech.kosp.global.dto.PageMeta;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleBookmarkRepository articleBookmarkRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    public ArticleListResponse getPosts(Long userId, Pageable pageable, User user) {
        User author = User.builder().id(userId).build();
        Page<Article> page = articleRepository.findByAuthor(author, pageable);

        return toArticleResponse(page, user);
    }

    public ArticleListResponse getBookmarks(Long userId, Pageable pageable, User user) {
        Page<Article> page = articleBookmarkRepository.findArticlesByUserId(userId, pageable);
        return toArticleResponse(page, user);
    }

    public CommentListResponse getComments(Long userId, Pageable pageable, User user) {
        Page<Comment> page = commentRepository.findByAuthorIdAndIsDeletedFalse(userId, pageable);
        return toCommentResponse(page, user);
    }

    public GithubActivityResponse getGithubActivities(Long userId) {
        User targetUser = userRepository.getById(userId);

        if (targetUser.getGithubUser() == null) {
            return GithubActivityResponse.empty();
        }

        // TODO: Implement after MongoDB schema is rebuilt
        return GithubActivityResponse.empty();
    }

    private ArticleListResponse toArticleResponse(Page<Article> page, User user) {
        List<ArticleResponse> posts = page.getContent().stream()
            .map(article -> ArticleResponse.from(
                article,
                isArticleLiked(user, article),
                isArticleBookmarked(user, article)
            ))
            .toList();
        return new ArticleListResponse<>(posts, PageMeta.from(page));
    }

    private CommentListResponse toCommentResponse(Page<Comment> page, User user) {
        List<CommentResponse> comments = page.getContent().stream()
            .map(comment -> CommentResponse.from(
                comment,
                isCommentLiked(user, comment),
                isCommentMine(user, comment)
            ))
            .toList();
        return CommentListResponse.from(comments, page);
    }

    private boolean isArticleLiked(User user, Article article) {
        return user != null && articleLikeRepository.existsByUserAndArticle(user, article);
    }

    private boolean isArticleBookmarked(User user, Article article) {
        return user != null && articleBookmarkRepository.existsByUserAndArticle(user, article);
    }

    private boolean isCommentLiked(User user, Comment comment) {
        return user != null && commentLikeRepository.existsByUserAndComment(user, comment);
    }

    private boolean isCommentMine(User user, Comment comment) {
        return user != null && comment.getAuthor().getId().equals(user.getId());
    }
}
