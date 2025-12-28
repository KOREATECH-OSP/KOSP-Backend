package kr.ac.koreatech.sw.kosp.domain.user.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleLikeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.model.Comment;
import kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentLikeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;
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
        Page<Comment> page = commentRepository.findByAuthorId(userId, pageable);
        return toCommentResponse(page, user);
    }

    private ArticleListResponse toArticleResponse(Page<Article> page, User user) {
        List<ArticleResponse> posts = page.getContent().stream()
            .map(article -> ArticleResponse.from(
                article, 
                isArticleLiked(user, article), 
                isArticleBookmarked(user, article)
            ))
            .toList();
        return new ArticleListResponse(posts, PageMeta.from(page));
    }

    private CommentListResponse toCommentResponse(Page<Comment> page, User user) {
        List<CommentResponse> comments = page.getContent().stream()
            .map(comment -> CommentResponse.from(
                comment,
                isCommentLiked(user, comment),
                isCommentMine(user, comment)
            ))
            .toList();
        return new CommentListResponse(comments, PageMeta.from(page));
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
